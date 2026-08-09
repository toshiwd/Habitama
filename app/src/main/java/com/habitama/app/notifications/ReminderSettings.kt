package com.habitama.app.notifications

import android.content.Context

data class ReminderSettings(
    val dailyEnabled: Boolean = false,
    val dailyHour: Int = 20,
    val dailyMinute: Int = 0,
    val monthlyReviewEnabled: Boolean = false,
)

class ReminderPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("reminder_settings", Context.MODE_PRIVATE)

    fun load(): ReminderSettings = ReminderSettings(
        dailyEnabled = preferences.getBoolean("daily_enabled", false),
        dailyHour = preferences.getInt("daily_hour", 20),
        dailyMinute = preferences.getInt("daily_minute", 0),
        monthlyReviewEnabled = preferences.getBoolean("monthly_review_enabled", false),
    )

    fun save(settings: ReminderSettings) {
        preferences.edit()
            .putBoolean("daily_enabled", settings.dailyEnabled)
            .putInt("daily_hour", settings.dailyHour)
            .putInt("daily_minute", settings.dailyMinute)
            .putBoolean("monthly_review_enabled", settings.monthlyReviewEnabled)
            .apply()
    }
}
