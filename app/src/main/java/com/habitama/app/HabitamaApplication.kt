package com.habitama.app

import android.app.Application
import androidx.room.Room
import com.habitama.app.data.HabitamaDatabase
import com.habitama.app.data.HabitamaRepository

class HabitamaApplication : Application() {
    lateinit var repository: HabitamaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            HabitamaDatabase::class.java,
            "habitama.db",
        ).build()
        repository = HabitamaRepository(database)
    }
}
