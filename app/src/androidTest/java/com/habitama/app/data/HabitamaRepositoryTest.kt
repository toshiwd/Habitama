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
        database = Room.inMemoryDatabaseBuilder(context, HabitamaDatabase::class.java).allowMainThreadQueries().build()
        repository = HabitamaRepository(database, fixedClock)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun threeGoalsCanBeReportedAndSameDayUpdateUsesPointDelta() = runBlocking {
        repository.createInitialGoals(
            listOf(
                GoalDraft("歩く", 6_000, "歩", GrowthType.VITALITY, "👣"),
                GoalDraft("単語", 5, "個", GrowthType.INTELLIGENCE, "📖"),
                GoalDraft("片づけ", 10, "分", GrowthType.DISCIPLINE, "🧹"),
            ),
        )
        val goals = database.goalDao().getActiveGoals("2026-08-10")
        repository.saveTodayRecords(goals.associate { it.id to it.targetValue })
        repository.saveTodayRecords(goals.associate { it.id to if (it.slotIndex == 0) 9_000L else it.targetValue })

        assertEquals(3, database.recordDao().count())
        assertEquals(32, database.recordDao().observeTotalEnergy().first())
        val stats = database.growthStatsDao().get()
        assertEquals(12, stats?.vitality)
        assertEquals(10, stats?.intelligence)
        assertEquals(10, stats?.discipline)
        assertEquals(32, stats?.totalPoints)
    }

    @Test
    fun scheduledGoalStartsTomorrowAndKeepsSnapshot() = runBlocking {
        repository.createInitialGoals(listOf(GoalDraft("歩く", 6_000, "歩")))
        val goal = database.goalDao().getActiveGoals("2026-08-10").single()
        repository.saveTodayRecords(mapOf(goal.id to 5_000))
        repository.scheduleGoalUpdate(goal.id, GoalDraft("運動する", 20, "分", GrowthType.VITALITY, "♥"))

        val todayGoal = database.goalDao().getActiveGoals("2026-08-10").single()
        val tomorrowGoal = database.goalDao().getActiveGoals("2026-08-11").single()
        val record = database.recordDao().getRecord("2026-08-10", goal.id)
        assertEquals("歩く", todayGoal.title)
        assertEquals("運動する", tomorrowGoal.title)
        assertNotNull(record)
        assertEquals(6_000L, record?.targetValueSnapshot)
    }
}
