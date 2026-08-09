package com.habitama.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GoalEntity::class, DailyGoalRecordEntity::class, GrowthStatsEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class HabitamaDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun recordDao(): DailyGoalRecordDao
    abstract fun growthStatsDao(): GrowthStatsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN slotIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE goals ADD COLUMN growthType TEXT NOT NULL DEFAULT 'DISCIPLINE'")
                db.execSQL("ALTER TABLE goals ADD COLUMN icon TEXT NOT NULL DEFAULT '✓'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_slotIndex ON goals(slotIndex)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_goal_records_new (
                        date TEXT NOT NULL,
                        goalId INTEGER NOT NULL,
                        actualValue INTEGER NOT NULL,
                        targetValueSnapshot INTEGER NOT NULL,
                        titleSnapshot TEXT NOT NULL,
                        unitSnapshot TEXT NOT NULL,
                        difficultySnapshot INTEGER NOT NULL,
                        evaluationScore REAL NOT NULL,
                        displayPercentage INTEGER NOT NULL,
                        energyEarned INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(date, goalId),
                        FOREIGN KEY(goalId) REFERENCES goals(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("INSERT INTO daily_goal_records_new SELECT * FROM daily_goal_records")
                db.execSQL("DROP TABLE daily_goal_records")
                db.execSQL("ALTER TABLE daily_goal_records_new RENAME TO daily_goal_records")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_goal_records_goalId ON daily_goal_records(goalId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_goal_records_date ON daily_goal_records(date)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS growth_stats (
                        id INTEGER NOT NULL,
                        vitality INTEGER NOT NULL,
                        intelligence INTEGER NOT NULL,
                        beauty INTEGER NOT NULL,
                        discipline INTEGER NOT NULL,
                        recovery INTEGER NOT NULL,
                        totalPoints INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO growth_stats
                    SELECT 1, 0, 0, 0, COALESCE(SUM(energyEarned), 0), 0,
                           COALESCE(SUM(energyEarned), 0), 0
                    FROM daily_goal_records
                    """.trimIndent(),
                )
            }
        }
    }
}
