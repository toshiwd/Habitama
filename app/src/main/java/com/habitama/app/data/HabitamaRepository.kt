package com.habitama.app.data

import androidx.room.withTransaction
import com.habitama.app.domain.MAX_INPUT_VALUE
import com.habitama.app.domain.evaluateProgress
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class GoalDraft(
    val title: String,
    val targetValue: Long,
    val unit: String,
)

data class DashboardData(
    val today: LocalDate,
    val activeGoal: GoalEntity?,
    val pendingGoal: GoalEntity?,
    val todayRecord: DailyGoalRecordEntity?,
    val totalEnergy: Int,
    val records: List<DailyGoalRecordEntity>,
)

class HabitamaRepository(
    private val database: HabitamaDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val goalDao = database.goalDao()
    private val recordDao = database.recordDao()

    fun today(): LocalDate = LocalDate.now(clock)

    fun observeDashboard(): Flow<DashboardData> {
        val today = today()
        val todayText = today.toString()
        val tomorrowText = today.plusDays(1).toString()
        val historyStart = today.minusDays(6).toString()
        return combine(
            goalDao.observeActiveGoal(todayText),
            goalDao.observeGoalStartingOn(tomorrowText),
            recordDao.observeRecord(todayText),
            recordDao.observeTotalEnergy(),
            recordDao.observeRange(historyStart, todayText),
        ) { activeGoal, pendingGoal, todayRecord, totalEnergy, records ->
            DashboardData(today, activeGoal, pendingGoal, todayRecord, totalEnergy, records)
        }
    }

    suspend fun createInitialGoal(draft: GoalDraft) {
        validateDraft(draft)
        database.withTransaction {
            check(goalDao.getActiveGoal(today().toString()) == null) { "今日の目標はすでに設定されています" }
            goalDao.insert(
                GoalEntity(
                    title = draft.title.trim(),
                    targetValue = draft.targetValue,
                    unit = draft.unit.trim(),
                    effectiveFrom = today().toString(),
                ),
            )
        }
    }

    suspend fun scheduleGoalUpdate(draft: GoalDraft) {
        validateDraft(draft)
        val today = today()
        val tomorrow = today.plusDays(1)
        database.withTransaction {
            val active = checkNotNull(goalDao.getActiveGoal(today.toString())) { "変更対象の目標がありません" }
            goalDao.deleteStartingOnOrAfter(tomorrow.toString())
            goalDao.setEffectiveTo(active.id, today.toString())
            goalDao.insert(
                GoalEntity(
                    title = draft.title.trim(),
                    targetValue = draft.targetValue,
                    unit = draft.unit.trim(),
                    effectiveFrom = tomorrow.toString(),
                ),
            )
        }
    }

    suspend fun saveTodayRecord(actualValue: Long): DailyGoalRecordEntity {
        require(actualValue in 0..MAX_INPUT_VALUE) { "実績値は0〜${MAX_INPUT_VALUE}で入力してください" }
        val today = today()
        return database.withTransaction {
            val goal = checkNotNull(goalDao.getActiveGoal(today.toString())) { "今日の目標がありません" }
            val evaluation = evaluateProgress(actualValue, goal.targetValue)
            val record = DailyGoalRecordEntity(
                date = today.toString(),
                goalId = goal.id,
                actualValue = actualValue,
                targetValueSnapshot = goal.targetValue,
                titleSnapshot = goal.title,
                unitSnapshot = goal.unit,
                difficultySnapshot = goal.difficulty,
                evaluationScore = evaluation.evaluationScore,
                displayPercentage = evaluation.displayPercentage,
                energyEarned = evaluation.energyEarned,
                updatedAtEpochMillis = clock.millis(),
            )
            recordDao.upsert(record)
            record
        }
    }

    private fun validateDraft(draft: GoalDraft) {
        require(draft.title.trim().isNotEmpty()) { "行動の名前を入力してください" }
        require(draft.title.trim().length <= 40) { "行動の名前は40文字以内で入力してください" }
        require(draft.targetValue in 1..MAX_INPUT_VALUE) { "目標値は1〜${MAX_INPUT_VALUE}で入力してください" }
        require(draft.unit.trim().isNotEmpty()) { "単位を入力してください" }
        require(draft.unit.trim().length <= 10) { "単位は10文字以内で入力してください" }
    }
}
