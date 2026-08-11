package com.habitama.app.calendar

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceCalendarEventTest {
    @Test
    fun timedEventIsMappedToEveryLocalDateItTouches() {
        val zone = ZoneId.of("Asia/Tokyo")
        val start = LocalDate.of(2026, 8, 11).atTime(23, 0).atZone(zone).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 8, 12).atTime(1, 0).atZone(zone).toInstant().toEpochMilli()
        val event = DeviceCalendarEvent(1, 2, "夜間作業", start, end, false, 0)

        assertEquals(listOf(LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 12)), event.dates(zone))
    }

    @Test
    fun allDayEndIsExclusiveAndUsesUtcDates() {
        val start = LocalDate.of(2026, 8, 11).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 8, 13).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val event = DeviceCalendarEvent(1, 2, "休暇", start, end, true, 0)

        assertEquals(listOf(LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 12)), event.dates(ZoneId.of("America/Los_Angeles")))
    }
}
