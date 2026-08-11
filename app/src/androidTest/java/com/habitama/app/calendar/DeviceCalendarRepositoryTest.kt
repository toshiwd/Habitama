package com.habitama.app.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class DeviceCalendarRepositoryTest {
    @Test
    fun settingsRoundTripWithoutStoringEventDetails() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("device_calendar_preferences", Context.MODE_PRIVATE).edit().clear().commit()
        val repository = DeviceCalendarRepository(context)

        assertFalse(repository.loadSettings().enabled)
        assertNull(repository.loadSettings().selectedCalendarIds)

        repository.setEnabled(true)
        repository.setSelectedCalendarIds(setOf(7L, 11L))

        assertTrue(repository.loadSettings().enabled)
        assertEquals(setOf(7L, 11L), repository.loadSettings().selectedCalendarIds)
    }

    @Test
    fun appRequestsReadCalendarButNeverWriteCalendar() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
        val requested = packageInfo.requestedPermissions.orEmpty().toSet()

        assertTrue(Manifest.permission.READ_CALENDAR in requested)
        assertFalse(Manifest.permission.WRITE_CALENDAR in requested)
    }

    @Test
    fun permissionStateChangesAfterRuntimeGrant() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = DeviceCalendarRepository(context)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.READ_CALENDAR)

        assertTrue(repository.hasReadPermission())
    }

    @Test
    fun visibleCalendarAndEventAreReadThroughCalendarProvider() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.adoptShellPermissionIdentity(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
        )
        val account = "habitama-test-${System.nanoTime()}"
        val calendarInsertUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()
        var calendarId: Long? = null
        var eventId: Long? = null
        try {
            calendarId = ContentUris.parseId(
                requireNotNull(
                    context.contentResolver.insert(
                        calendarInsertUri,
                        ContentValues().apply {
                            put(CalendarContract.Calendars.ACCOUNT_NAME, account)
                            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                            put(CalendarContract.Calendars.NAME, account)
                            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Habitama Provider Test")
                            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xff6750a4.toInt())
                            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
                            put(CalendarContract.Calendars.OWNER_ACCOUNT, account)
                            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
                            put(CalendarContract.Calendars.VISIBLE, 1)
                            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, "Asia/Tokyo")
                        },
                    ),
                ),
            )
            val insertedCalendarId = requireNotNull(calendarId)
            val date = LocalDate.of(2026, 8, 11)
            val zone = ZoneId.of("Asia/Tokyo")
            val start = date.atTime(15, 0).atZone(zone).toInstant().toEpochMilli()
            val end = date.atTime(16, 0).atZone(zone).toInstant().toEpochMilli()
            eventId = ContentUris.parseId(
                requireNotNull(
                    context.contentResolver.insert(
                        CalendarContract.Events.CONTENT_URI,
                        ContentValues().apply {
                            put(CalendarContract.Events.CALENDAR_ID, insertedCalendarId)
                            put(CalendarContract.Events.TITLE, "Habitama Test Event")
                            put(CalendarContract.Events.DTSTART, start)
                            put(CalendarContract.Events.DTEND, end)
                            put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Tokyo")
                        },
                    ),
                ),
            )

            val repository = DeviceCalendarRepository(context)
            assertTrue(repository.listVisibleCalendars().any { it.id == insertedCalendarId && it.displayName == "Habitama Provider Test" })
            val events = repository.eventsBetween(date, date.plusDays(1), setOf(insertedCalendarId), zone)
            assertTrue(events.any { it.eventId == eventId && it.title == "Habitama Test Event" })
        } finally {
            eventId?.let { context.contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, it), null, null) }
            calendarId?.let {
                val deleteUri = ContentUris.withAppendedId(calendarInsertUri, it)
                context.contentResolver.delete(deleteUri, null, null)
            }
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }
}
