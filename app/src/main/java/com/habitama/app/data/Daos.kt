package com.habitama.app.data

import androidx.room.Dao
import androidx.room.Insert
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
        ORDER BY slotIndex, effectiveFrom DESC, id DESC
        """,
    )
    fun observeActiveGoals(date: String): Flow<List<GoalEntity>>

    @Query(
        """
        SELECT * FROM goals
        WHERE effectiveFrom <= :date
          AND (effectiveTo IS NULL OR effectiveTo >= :date)
        ORDER BY slotIndex, effectiveFrom DESC, id DESC
        """,
    )
    suspend fun getActiveGoals(date: String): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE effectiveFrom = :date ORDER BY slotIndex")
    fun observeGoalsStartingOn(date: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getById(goalId: Long): GoalEntity?

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Query("UPDATE goals SET effectiveTo = :effectiveTo WHERE id = :goalId")
    suspend fun setEffectiveTo(goalId: Long, effectiveTo: String)

    @Query("DELETE FROM goals WHERE slotIndex = :slotIndex AND effectiveFrom >= :date")
    suspend fun deleteStartingOnOrAfter(slotIndex: Int, date: String)
}

@Dao
interface DailyGoalRecordDao {
    @Query("SELECT * FROM daily_goal_records WHERE date = :date ORDER BY goalId")
    fun observeRecords(date: String): Flow<List<DailyGoalRecordEntity>>

    @Query("SELECT * FROM daily_goal_records WHERE date = :date AND goalId = :goalId LIMIT 1")
    suspend fun getRecord(date: String, goalId: Long): DailyGoalRecordEntity?

    @Query("SELECT * FROM daily_goal_records WHERE date BETWEEN :start AND :end ORDER BY date DESC, goalId")
    fun observeRange(start: String, end: String): Flow<List<DailyGoalRecordEntity>>

    @Query("SELECT COALESCE(SUM(energyEarned), 0) FROM daily_goal_records")
    fun observeTotalEnergy(): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_goal_records")
    suspend fun count(): Int

    @Query("DELETE FROM daily_goal_records WHERE date = :date AND goalId = :goalId")
    suspend fun delete(date: String, goalId: Long)

    @Upsert
    suspend fun upsert(record: DailyGoalRecordEntity)
}

@Dao
interface GrowthStatsDao {
    @Query("SELECT * FROM growth_stats WHERE id = 1")
    fun observe(): Flow<GrowthStatsEntity?>

    @Query("SELECT * FROM growth_stats WHERE id = 1")
    suspend fun get(): GrowthStatsEntity?

    @Upsert
    suspend fun upsert(stats: GrowthStatsEntity)
}
