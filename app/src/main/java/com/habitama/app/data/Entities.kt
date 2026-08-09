package com.habitama.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals",
    indices = [Index("effectiveFrom"), Index("effectiveTo")],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetValue: Long,
    val unit: String,
    val difficulty: Int = 3,
    val effectiveFrom: String,
    val effectiveTo: String? = null,
)

@Entity(
    tableName = "daily_goal_records",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("goalId")],
)
data class DailyGoalRecordEntity(
    @PrimaryKey val date: String,
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
)
