package com.habitama.app.notifications

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderSchedulerTest {
    private val zone = ZoneId.of("Asia/Tokyo")

    @Test
    fun dailyTriggerUsesTodayBeforeTimeAndTomorrowAfterTime() {
        val morning = ZonedDateTime.of(2026, 8, 10, 9, 0, 0, 0, zone)
        val night = ZonedDateTime.of(2026, 8, 10, 21, 0, 0, 0, zone)
        assertEquals("2026-08-10T20:00+09:00[Asia/Tokyo]", ReminderScheduler.nextDailyTrigger(morning, 20, 0).toString())
        assertEquals("2026-08-11T20:00+09:00[Asia/Tokyo]", ReminderScheduler.nextDailyTrigger(night, 20, 0).toString())
    }

    @Test
    fun monthlyTriggerUsesNextFirstDayAtTen() {
        val now = ZonedDateTime.of(2026, 8, 10, 9, 0, 0, 0, zone)
        assertEquals("2026-09-01T10:00+09:00[Asia/Tokyo]", ReminderScheduler.nextMonthlyTrigger(now).toString())
    }
}
