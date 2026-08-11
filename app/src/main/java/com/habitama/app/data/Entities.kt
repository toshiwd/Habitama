package com.habitama.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.habitama.app.domain.GoalEvaluationMode

object GrowthType {
    const val VITALITY = "VITALITY"
    const val INTELLIGENCE = "INTELLIGENCE"
    const val BEAUTY = "BEAUTY"
    const val DISCIPLINE = "DISCIPLINE"
    const val RECOVERY = "RECOVERY"
    val all = listOf(VITALITY, INTELLIGENCE, BEAUTY, DISCIPLINE, RECOVERY)
}

@Entity(
    tableName = "goals",
    indices = [Index("effectiveFrom"), Index("effectiveTo"), Index("slotIndex")],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetValue: Long,
    val unit: String,
    val difficulty: Int = 3,
    val effectiveFrom: String,
    val effectiveTo: String? = null,
    val slotIndex: Int = 0,
    val growthType: String = GrowthType.DISCIPLINE,
    val icon: String = "✓",
    val evaluationMode: String = GoalEvaluationMode.AT_LEAST,
)

@Entity(
    tableName = "daily_goal_records",
    primaryKeys = ["date", "goalId"],
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("goalId"), Index("date")],
)
data class DailyGoalRecordEntity(
    val date: String,
    val goalId: Long,
    val actualValue: Long,
    val targetValueSnapshot: Long,
    val titleSnapshot: String,
    val unitSnapshot: String,
    val difficultySnapshot: Int,
    val evaluationScore: Double,
    val displayPercentage: Int,
    val energyEarned: Int,
    val updatedAtEpochMillis: Long,
    val evaluationModeSnapshot: String = GoalEvaluationMode.AT_LEAST,
)

@Entity(tableName = "growth_stats")
data class GrowthStatsEntity(
    @PrimaryKey val id: Int = 1,
    val vitality: Int = 0,
    val intelligence: Int = 0,
    val beauty: Int = 0,
    val discipline: Int = 0,
    val recovery: Int = 0,
    val totalPoints: Int = 0,
    val updatedAtEpochMillis: Long = 0,
) {
    fun valueOf(type: String): Int = when (type) {
        GrowthType.VITALITY -> vitality
        GrowthType.INTELLIGENCE -> intelligence
        GrowthType.BEAUTY -> beauty
        GrowthType.RECOVERY -> recovery
        else -> discipline
    }
}
