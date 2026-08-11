package com.habitama.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.habitama.app.calendar.DeviceCalendarEvent
import com.habitama.app.ui.theme.HabitamaTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun selectedDayShowsReadOnlyDeviceCalendarAgenda() {
        val date = LocalDate.of(2026, 8, 11)
        val zone = ZoneId.systemDefault()
        val event = DeviceCalendarEvent(
            eventId = 91,
            calendarId = 7,
            title = "歯医者",
            startMillis = date.atTime(15, 0).atZone(zone).toInstant().toEpochMilli(),
            endMillis = date.atTime(16, 0).atZone(zone).toInstant().toEpochMilli(),
            allDay = false,
            color = 0xff6750a4.toInt(),
        )
        val state = HabitamaUiState(
            isLoading = false,
            today = date,
            deviceCalendar = DeviceCalendarUiState(
                enabled = true,
                permissionGranted = true,
                selectedCalendarIds = setOf(7),
                events = listOf(event),
            ),
        )
        composeRule.setContent {
            HabitamaTheme { CalendarScreen(state, PaddingValues(0.dp)) }
        }

        composeRule.onNodeWithText("8月11日の予定").assertExists()
        composeRule.onNodeWithText("歯医者").assertExists()
        composeRule.onNodeWithText("15:00〜16:00").assertExists()
    }

    @Test
    fun calendarCanMoveToPastAndFutureAndReturnToToday() {
        val today = LocalDate.of(2026, 8, 11)
        val requestedMonths = mutableListOf<YearMonth>()
        composeRule.setContent {
            HabitamaTheme {
                CalendarScreen(
                    state = HabitamaUiState(isLoading = false, today = today),
                    padding = PaddingValues(0.dp),
                    onVisibleMonthChanged = { requestedMonths += it },
                )
            }
        }

        composeRule.waitUntil { requestedMonths.lastOrNull() == YearMonth.of(2026, 8) }
        composeRule.onNodeWithTag("calendar_previous_month").performClick()
        composeRule.onNodeWithText("2026年 7月").assertExists()
        composeRule.waitUntil { requestedMonths.lastOrNull() == YearMonth.of(2026, 7) }

        composeRule.onNodeWithTag("calendar_next_month").performClick()
        composeRule.onNodeWithTag("calendar_next_month").performClick()
        composeRule.onNodeWithText("2026年 9月").assertExists()
        composeRule.waitUntil { requestedMonths.lastOrNull() == YearMonth.of(2026, 9) }

        composeRule.onNodeWithTag("calendar_today").performClick()
        composeRule.onNodeWithText("2026年 8月").assertExists()
        composeRule.waitUntil { requestedMonths.lastOrNull() == YearMonth.of(2026, 8) }
        assertEquals(YearMonth.of(2026, 8), requestedMonths.last())
    }
}
