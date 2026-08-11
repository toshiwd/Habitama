package com.habitama.app.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitamaMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HabitamaDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2PreservesGoalRecordAndSeedsGrowth() {
        val dbName = "habitama-migration-1-2-test"
        helper.createDatabase(dbName, 1).apply {
            execSQL("INSERT INTO goals VALUES (1, '歩く', 6000, '歩', 3, '2026-08-10', NULL)")
            execSQL("INSERT INTO daily_goal_records VALUES ('2026-08-10', 1, 5000, 6000, '歩く', '歩', 3, 0.8333, 83, 8, 1)")
            close()
        }

        helper.runMigrationsAndValidate(dbName, 2, true, HabitamaDatabase.MIGRATION_1_2).use { db ->
            db.query("SELECT title, slotIndex, growthType FROM goals").use { cursor ->
                cursor.moveToFirst()
                assertEquals("歩く", cursor.getString(0))
                assertEquals(0, cursor.getInt(1))
                assertEquals(GrowthType.DISCIPLINE, cursor.getString(2))
            }
            db.query("SELECT discipline, totalPoints FROM growth_stats WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals(8, cursor.getInt(0))
                assertEquals(8, cursor.getInt(1))
            }
        }
    }

    @Test
    fun migrate2To3AddsAtLeastDefaultsWithoutChangingExistingData() {
        val dbName = "habitama-migration-2-3-test"
        helper.createDatabase(dbName, 2).apply {
            execSQL("INSERT INTO goals VALUES (1, '歩く', 6000, '歩', 3, '2026-08-11', NULL, 0, 'VITALITY', 'walk')")
            execSQL("INSERT INTO daily_goal_records VALUES ('2026-08-11', 1, 5000, 6000, '歩く', '歩', 3, 0.8333, 83, 8, 1)")
            close()
        }

        helper.runMigrationsAndValidate(dbName, 3, true, HabitamaDatabase.MIGRATION_2_3).use { db ->
            db.query("SELECT title, evaluationMode FROM goals").use { cursor ->
                cursor.moveToFirst()
                assertEquals("歩く", cursor.getString(0))
                assertEquals("AT_LEAST", cursor.getString(1))
            }
            db.query("SELECT actualValue, evaluationModeSnapshot FROM daily_goal_records").use { cursor ->
                cursor.moveToFirst()
                assertEquals(5000, cursor.getInt(0))
                assertEquals("AT_LEAST", cursor.getString(1))
            }
        }
    }
}
