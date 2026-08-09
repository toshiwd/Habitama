package com.habitama.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query(
        """
        SELECT * FROM goals
        WHERE effectiveFrom <= :date
          AND (effectiveTo IS NULL OR effectiveTo >= :date)
        ORDER BY effectiveFrom DESC, id DESC
        LIMIT 1
        """,
    )
    fun observeActiveGoal(date: String): Flow<GoalEntity?>

    @Query(
        """
        SELECT * FROM goals
        WHERE effectiveFrom <= :date
          AND (effectiveTo IS NULL OR effectiveTo >= :date)
        ORDER BY effectiveFrom DESC, id DESC
        LIMIT 1
        """,
    )
    suspend fun getActiveGoal(date: String): GoalEntity?

    @Query("SELECT * FROM goals WHERE effectiveFrom = :date ORDER BY id DESC LIMIT 1")
    fun observeGoalStartingOn(date: String): Flow<GoalEntity?>

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Query("UPDATE goals SET effectiveTo = :effectiveTo WHERE id = :goalId")
    suspend fun setEffectiveTo(goalId: Long, effectiveTo: String)

    @Query("DELETE FROM goals WHERE effectiveFrom >= :date")
    suspend fun deleteStartingOnOrAfter(date: String)
}

@Dao
interface DailyGoalRecordDao {
    @Query("SELECT * FROM daily_goal_records WHERE date = :date")
    fun observeRecord(date: String): Flow<DailyGoalRecordEntity?>

    @Query("SELECT * FROM daily_goal_records WHERE date = :date")
    suspend fun getRecord(date: String): DailyGoalRecordEntity?

    @Query("SELECT * FROM daily_goal_records WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun observeRange(start: String, end: String): Flow<List<DailyGoalRecordEntity>>

    @Query("SELECT COALESCE(SUM(energyEarned), 0) FROM daily_goal_records")
    fun observeTotalEnergy(): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_goal_records")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(record: DailyGoalRecordEntity)
}
