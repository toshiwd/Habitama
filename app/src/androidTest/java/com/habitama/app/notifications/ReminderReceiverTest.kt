package com.habitama.app.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderReceiverTest {
    @Test
    fun dailyReminderPostsNotificationAfterPermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}")
            .close()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancelAll()

        ReminderReceiver().onReceive(
            context,
            Intent(context, ReminderReceiver::class.java)
                .putExtra(ReminderScheduler.EXTRA_KIND, ReminderScheduler.KIND_DAILY),
        )

        assertTrue(manager.activeNotifications.any { it.id == 4101 })
        manager.cancelAll()
    }
}
