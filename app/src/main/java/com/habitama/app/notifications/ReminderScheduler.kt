package com.habitama.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZonedDateTime

object ReminderScheduler {
    const val EXTRA_KIND = "reminder_kind"
    const val KIND_DAILY = "daily"
    const val KIND_MONTHLY = "monthly"
    private const val DAILY_REQUEST = 3101
    private const val MONTHLY_REQUEST = 3102

    fun scheduleAll(context: Context, settings: ReminderSettings = ReminderPreferences(context).load()) {
        scheduleDaily(context, settings)
        scheduleMonthlyReview(context, settings)
    }

    fun scheduleDaily(context: Context, settings: ReminderSettings) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val intent = pendingIntent(context, KIND_DAILY, DAILY_REQUEST)
        alarm.cancel(intent)
        if (!settings.dailyEnabled) return
        val trigger = nextDailyTrigger(ZonedDateTime.now(), settings.dailyHour, settings.dailyMinute)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toInstant().toEpochMilli(), intent)
    }

    fun scheduleMonthlyReview(context: Context, settings: ReminderSettings) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val intent = pendingIntent(context, KIND_MONTHLY, MONTHLY_REQUEST)
        alarm.cancel(intent)
        if (!settings.monthlyReviewEnabled) return
        val trigger = nextMonthlyTrigger(ZonedDateTime.now())
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toInstant().toEpochMilli(), intent)
    }

    internal fun nextDailyTrigger(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
        val today = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    internal fun nextMonthlyTrigger(now: ZonedDateTime): ZonedDateTime {
        val thisMonth = now.withDayOfMonth(1).withHour(10).withMinute(0).withSecond(0).withNano(0)
        return if (thisMonth.isAfter(now)) thisMonth else thisMonth.plusMonths(1)
    }

    private fun pendingIntent(context: Context, kind: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
