package com.habitama.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.habitama.app.MainActivity
import com.habitama.app.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra(ReminderScheduler.EXTRA_KIND) ?: ReminderScheduler.KIND_DAILY
        NotificationChannels.create(context)
        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (canNotify) {
            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val monthly = kind == ReminderScheduler.KIND_MONTHLY
            val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(if (monthly) "今月の行動を見直しませんか" else "今日の行動を記録しましょう")
                .setContentText(if (monthly) "無理なく続けられる目標か、月に一度だけ確認しましょう。" else "できた分だけで大丈夫。ハビタマに今日の成長を伝えましょう。")
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            context.getSystemService(NotificationManager::class.java).notify(if (monthly) 4102 else 4101, notification)
        }
        val settings = ReminderPreferences(context).load()
        if (kind == ReminderScheduler.KIND_MONTHLY) ReminderScheduler.scheduleMonthlyReview(context, settings)
        else ReminderScheduler.scheduleDaily(context, settings)
    }
}

object NotificationChannels {
    const val REMINDERS = "habitama_reminders"

    fun create(context: Context) {
        val channel = NotificationChannel(REMINDERS, "行動のリマインダー", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "毎日の報告と月1回の目標見直しをお知らせします"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
