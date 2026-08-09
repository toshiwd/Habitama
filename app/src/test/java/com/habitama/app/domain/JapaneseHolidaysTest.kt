package com.habitama.app.domain

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JapaneseHolidaysTest {
    @Test
    fun holidaysFor2026IncludeEquinoxHappyMondayAndSubstitute() {
        val holidays = JapaneseHolidays.holidays(2026)
        assertTrue(LocalDate.of(2026, 3, 20) in holidays)
        assertTrue(LocalDate.of(2026, 9, 21) in holidays)
        assertTrue(LocalDate.of(2026, 5, 6) in holidays)
        assertTrue(LocalDate.of(2026, 11, 23) in holidays)
        assertFalse(LocalDate.of(2026, 11, 24) in holidays)
    }
}
