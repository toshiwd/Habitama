package com.habitama.app

import android.app.Application
import androidx.room.Room
import com.habitama.app.data.HabitamaDatabase
import com.habitama.app.data.HabitamaRepository
import com.habitama.app.notifications.NotificationChannels
import com.habitama.app.notifications.ReminderScheduler

class HabitamaApplication : Application() {
    lateinit var repository: HabitamaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            HabitamaDatabase::class.java,
            "habitama.db",
        ).addMigrations(HabitamaDatabase.MIGRATION_1_2, HabitamaDatabase.MIGRATION_2_3).build()
        repository = HabitamaRepository(database)
        NotificationChannels.create(this)
        ReminderScheduler.scheduleAll(this)
    }
}
