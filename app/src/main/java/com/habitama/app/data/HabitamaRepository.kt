package com.habitama.app.data

import androidx.room.withTransaction
import com.habitama.app.domain.MAX_INPUT_VALUE
import com.habitama.app.domain.GoalEvaluationMode
import com.habitama.app.domain.evaluateProgress
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

const val MAX_ACTIVE_GOALS = 10

data class GoalDraft(
    val title: String,
    val targetValue: Long,
    val unit: String,
    val growthType: String = GrowthType.DISCIPLINE,
    val icon: String = "✓",
    val evaluationMode: String = GoalEvaluationMode.AT_LEAST,
)

data class GoalGain(val goalId: Long, val title: String, val growthType: String, val points: Int)

data class ReportResult(
    val records: List<DailyGoalRecordEntity>,
    val gains: List<GoalGain>,
    val totalEarned: Int,
)

data class DashboardData(
    val today: LocalDate,
    val activeGoals: List<GoalEntity>,
    val pendingGoals: List<GoalEntity>,
    val todayRecords: List<DailyGoalRecordEntity>,
    val totalEnergy: Int,
    val growthStats: GrowthStatsEntity,
    val records: List<DailyGoalRecordEntity>,
)

private data class DashboardCore(
    val activeGoals: List<GoalEntity>,
    val pendingGoals: List<GoalEntity>,
    val todayRecords: List<DailyGoalRecordEntity>,
    val totalEnergy: Int,
    val growthStats: GrowthStatsEntity,
)

class HabitamaRepository(
    private val database: HabitamaDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val goalDao = database.goalDao()
    private val recordDao = database.recordDao()
    private val statsDao = database.growthStatsDao()

    fun today(): LocalDate = LocalDate.now(clock)

    fun observeDashboard(): Flow<DashboardData> {
        val today = today()
        val todayText = today.toString()
        val tomorrowText = today.plusDays(1).toString()
        val core = combine(
            goalDao.observeActiveGoals(todayText),
            goalDao.observeGoalsStartingOn(tomorrowText),
            recordDao.observeRecords(todayText),
            recordDao.observeTotalEnergy(),
            statsDao.observe(),
        ) { activeGoals, pendingGoals, todayRecords, totalEnergy, stats ->
            DashboardCore(
                activeGoals.distinctBy { it.slotIndex }.take(MAX_ACTIVE_GOALS),
                pendingGoals,
                todayRecords,
                totalEnergy,
                stats ?: GrowthStatsEntity(totalPoints = totalEnergy),
            )
        }
        return core.combine(recordDao.observeAll()) { current, records ->
            DashboardData(
                today = today,
                activeGoals = current.activeGoals,
                pendingGoals = current.pendingGoals,
                todayRecords = current.todayRecords,
                totalEnergy = current.totalEnergy,
                growthStats = current.growthStats,
                records = records,
            )
        }
    }

    suspend fun createInitialGoals(drafts: List<GoalDraft>) {
        require(drafts.isNotEmpty()) { "行動を1つ以上選んでください" }
        require(drafts.size <= MAX_ACTIVE_GOALS) { "行動は${MAX_ACTIVE_GOALS}件までです" }
        drafts.forEach(::validateDraft)
        database.withTransaction {
            check(goalDao.getActiveGoals(today().toString()).isEmpty()) { "今日の行動はすでに設定されています" }
            drafts.forEachIndexed { slot, draft -> insertGoal(draft, slot, today()) }
            statsDao.upsert(GrowthStatsEntity(updatedAtEpochMillis = clock.millis()))
        }
    }

    suspend fun addGoal(draft: GoalDraft) {
        validateDraft(draft)
        database.withTransaction {
            val active = goalDao.getActiveGoals(today().toString()).distinctBy { it.slotIndex }
            check(active.size < MAX_ACTIVE_GOALS) { "行動は${MAX_ACTIVE_GOALS}件までです" }
            val slot = (0 until MAX_ACTIVE_GOALS).first { candidate -> active.none { it.slotIndex == candidate } }
            insertGoal(draft, slot, today())
        }
    }

    suspend fun updateGoalNow(goalId: Long, draft: GoalDraft) {
        validateDraft(draft)
        val today = today()
        database.withTransaction {
            val active = checkNotNull(goalDao.getById(goalId)) { "変更対象の行動がありません" }
            check(active.effectiveFrom <= today.toString() && (active.effectiveTo == null || active.effectiveTo >= today.toString())) {
                "この行動は現在有効ではありません"
            }
            goalDao.deleteStartingOnOrAfter(active.slotIndex, today.plusDays(1).toString())
            goalDao.setEffectiveTo(active.id, today.minusDays(1).toString())
            val replacementId = insertGoal(draft, active.slotIndex, today)

            val existingRecord = recordDao.getRecord(today.toString(), active.id)
            if (existingRecord != null) {
                recordDao.delete(today.toString(), active.id)
                recordDao.upsert(existingRecord.copy(goalId = replacementId))
                if (active.growthType != draft.growthType) {
                    val stats = statsDao.get() ?: GrowthStatsEntity()
                    statsDao.upsert(
                        stats.add(active.growthType, -existingRecord.energyEarned)
                            .add(draft.growthType, existingRecord.energyEarned)
                            .copy(updatedAtEpochMillis = clock.millis()),
                    )
                }
            }
        }
    }

    suspend fun saveTodayRecords(actualValues: Map<Long, Long>): ReportResult {
        actualValues.values.forEach { actual ->
            require(actual in 0..MAX_INPUT_VALUE) { "実績値は0〜$MAX_INPUT_VALUE で入力してください" }
        }
        val today = today()
        return database.withTransaction {
            val goals = goalDao.getActiveGoals(today.toString()).distinctBy { it.slotIndex }.take(MAX_ACTIVE_GOALS)
            check(goals.isNotEmpty()) { "今日の行動がありません" }
            check(goals.all { actualValues.containsKey(it.id) }) { "すべての行動を入力してください" }

            val records = mutableListOf<DailyGoalRecordEntity>()
            val gains = mutableListOf<GoalGain>()
            var stats = statsDao.get() ?: GrowthStatsEntity()
            goals.forEach { goal ->
                val actual = checkNotNull(actualValues[goal.id])
                val evaluation = evaluateProgress(actual, goal.targetValue, goal.evaluationMode)
                val previous = recordDao.getRecord(today.toString(), goal.id)
                val delta = evaluation.energyEarned - (previous?.energyEarned ?: 0)
                val record = DailyGoalRecordEntity(
                    date = today.toString(),
                    goalId = goal.id,
                    actualValue = actual,
                    targetValueSnapshot = goal.targetValue,
                    titleSnapshot = goal.title,
                    unitSnapshot = goal.unit,
                    difficultySnapshot = goal.difficulty,
                    evaluationScore = evaluation.evaluationScore,
                    displayPercentage = evaluation.displayPercentage,
                    energyEarned = evaluation.energyEarned,
                    updatedAtEpochMillis = clock.millis(),
                    evaluationModeSnapshot = goal.evaluationMode,
                )
                recordDao.upsert(record)
                records += record
                gains += GoalGain(goal.id, goal.title, goal.growthType, delta)
                stats = stats.add(goal.growthType, delta)
            }
            statsDao.upsert(stats.copy(updatedAtEpochMillis = clock.millis()))
            ReportResult(records, gains, records.sumOf { it.energyEarned })
        }
    }

    private suspend fun insertGoal(draft: GoalDraft, slot: Int, date: LocalDate): Long =
        goalDao.insert(
            GoalEntity(
                title = draft.title.trim(),
                targetValue = draft.targetValue,
                unit = draft.unit.trim(),
                effectiveFrom = date.toString(),
                slotIndex = slot,
                growthType = draft.growthType,
                icon = draft.icon.take(4),
                evaluationMode = draft.evaluationMode,
            ),
        )

    private fun validateDraft(draft: GoalDraft) {
        require(draft.title.trim().isNotEmpty()) { "行動の名前を入力してください" }
        require(draft.title.trim().length <= 40) { "行動の名前は40文字以内で入力してください" }
        require(draft.targetValue in 1..MAX_INPUT_VALUE) { "目標値は1〜$MAX_INPUT_VALUE で入力してください" }
        require(draft.unit.trim().isNotEmpty()) { "単位を入力してください" }
        require(draft.unit.trim().length <= 10) { "単位は10文字以内で入力してください" }
        require(draft.growthType in GrowthType.all) { "成長タイプが不正です" }
        require(draft.evaluationMode in GoalEvaluationMode.all) { "評価方法が不正です" }
    }
}

private fun GrowthStatsEntity.add(type: String, delta: Int): GrowthStatsEntity = when (type) {
    GrowthType.VITALITY -> copy(vitality = (vitality + delta).coerceAtLeast(0), totalPoints = (totalPoints + delta).coerceAtLeast(0))
    GrowthType.INTELLIGENCE -> copy(intelligence = (intelligence + delta).coerceAtLeast(0), totalPoints = (totalPoints + delta).coerceAtLeast(0))
    GrowthType.BEAUTY -> copy(beauty = (beauty + delta).coerceAtLeast(0), totalPoints = (totalPoints + delta).coerceAtLeast(0))
    GrowthType.RECOVERY -> copy(recovery = (recovery + delta).coerceAtLeast(0), totalPoints = (totalPoints + delta).coerceAtLeast(0))
    else -> copy(discipline = (discipline + delta).coerceAtLeast(0), totalPoints = (totalPoints + delta).coerceAtLeast(0))
}
