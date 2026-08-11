package com.habitama.app.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

data class DeviceCalendar(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val color: Int,
)

data class DeviceCalendarEvent(
    val eventId: Long,
    val calendarId: Long,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val allDay: Boolean,
    val color: Int,
) {
    fun dates(zoneId: ZoneId = ZoneId.systemDefault()): List<LocalDate> {
        val eventZone = if (allDay) ZoneOffset.UTC else zoneId
        val start = Instant.ofEpochMilli(startMillis).atZone(eventZone).toLocalDate()
        val inclusiveEndMillis = (endMillis - 1).coerceAtLeast(startMillis)
        val end = Instant.ofEpochMilli(inclusiveEndMillis).atZone(eventZone).toLocalDate()
        return generateSequence(start) { current -> current.plusDays(1).takeIf { it <= end } }.toList()
    }
}

data class DeviceCalendarSettings(
    val enabled: Boolean,
    val selectedCalendarIds: Set<Long>?,
)

class DeviceCalendarRepository(private val context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun hasReadPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    fun loadSettings(): DeviceCalendarSettings = DeviceCalendarSettings(
        enabled = preferences.getBoolean(KEY_ENABLED, false),
        selectedCalendarIds = if (preferences.contains(KEY_SELECTED_IDS)) {
            preferences.getStringSet(KEY_SELECTED_IDS, emptySet()).orEmpty().mapNotNull(String::toLongOrNull).toSet()
        } else {
            null
        },
    )

    fun setEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun setSelectedCalendarIds(ids: Set<Long>) {
        preferences.edit().putStringSet(KEY_SELECTED_IDS, ids.map(Long::toString).toSet()).apply()
    }

    fun listVisibleCalendars(): List<DeviceCalendar> {
        if (!hasReadPermission()) return emptyList()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
        )
        return context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} COLLATE NOCASE",
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            val colorIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        DeviceCalendar(
                            id = cursor.getLong(idIndex),
                            displayName = cursor.getString(nameIndex).orEmpty().ifBlank { "名称なし" },
                            accountName = cursor.getString(accountIndex).orEmpty(),
                            color = cursor.getInt(colorIndex),
                        ),
                    )
                }
            }
        }.orEmpty()
    }

    fun eventsBetween(
        start: LocalDate,
        endExclusive: LocalDate,
        calendarIds: Set<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<DeviceCalendarEvent> {
        if (!hasReadPermission() || calendarIds.isEmpty()) return emptyList()
        val beginMillis = start.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = endExclusive.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also { builder ->
            ContentUris.appendId(builder, beginMillis)
            ContentUris.appendId(builder, endMillis)
        }.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.DISPLAY_COLOR,
        )
        val placeholders = calendarIds.joinToString(",") { "?" }
        return context.contentResolver.query(
            uri,
            projection,
            "${CalendarContract.Instances.CALENDAR_ID} IN ($placeholders)",
            calendarIds.map(Long::toString).toTypedArray(),
            "${CalendarContract.Instances.BEGIN} ASC, ${CalendarContract.Instances.END} ASC",
        )?.use { cursor ->
            val eventIdIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val calendarIdIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val colorIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DISPLAY_COLOR)
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        DeviceCalendarEvent(
                            eventId = cursor.getLong(eventIdIndex),
                            calendarId = cursor.getLong(calendarIdIndex),
                            title = cursor.getString(titleIndex).orEmpty().ifBlank { "タイトルなし" },
                            startMillis = cursor.getLong(beginIndex),
                            endMillis = cursor.getLong(endIndex),
                            allDay = cursor.getInt(allDayIndex) == 1,
                            color = cursor.getInt(colorIndex),
                        ),
                    )
                }
            }
        }.orEmpty()
    }

    companion object {
        private const val PREFERENCES_NAME = "device_calendar_preferences"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_SELECTED_IDS = "selected_calendar_ids"
    }
}
