package com.habitama.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitamaRepositoryTest {
    private lateinit var database: HabitamaDatabase
    private lateinit var repository: HabitamaRepository
    private val fixedClock = Clock.fixed(Instant.parse("2026-08-10T03:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HabitamaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HabitamaRepository(database, fixedClock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun sameDayUpdateReplacesRecordAndEnergy() = runBlocking {
        repository.createInitialGoal(GoalDraft("歩く", 6_000, "歩"))
        repository.saveTodayRecord(5_000)
        repository.saveTodayRecord(10_000)

        assertEquals(1, database.recordDao().count())
        assertEquals(12, database.recordDao().observeTotalEnergy().first())
        val record = database.recordDao().getRecord("2026-08-10")
        assertNotNull(record)
        assertEquals(10_000L, record?.actualValue)
        assertEquals(6_000L, record?.targetValueSnapshot)
    }

    @Test
    fun scheduledGoalStartsTomorrowAndKeepsSnapshot() = runBlocking {
        repository.createInitialGoal(GoalDraft("歩く", 6_000, "歩"))
        repository.saveTodayRecord(5_000)
        repository.scheduleGoalUpdate(GoalDraft("運動する", 20, "分"))

        val todayGoal = database.goalDao().getActiveGoal("2026-08-10")
        val tomorrowGoal = database.goalDao().getActiveGoal("2026-08-11")
        val record = database.recordDao().getRecord("2026-08-10")
        assertEquals("歩く", todayGoal?.title)
        assertEquals("運動する", tomorrowGoal?.title)
        assertEquals(6_000L, record?.targetValueSnapshot)
        assertEquals("歩", record?.unitSnapshot)
    }
}
