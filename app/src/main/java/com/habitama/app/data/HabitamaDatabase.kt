package com.habitama.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GoalEntity::class, DailyGoalRecordEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class HabitamaDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun recordDao(): DailyGoalRecordDao
}
