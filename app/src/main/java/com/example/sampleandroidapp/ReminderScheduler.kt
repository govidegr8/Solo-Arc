package com.example.sampleandroidapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    fun prepare(tasks: List<Task>, dailyReminderTime: String) {
        createChannel()
        val alarm = context.getSystemService(AlarmManager::class.java)
        tasks.forEach { task ->
            val time = task.time.split(":").map(String::toInt)
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time[0])
                set(Calendar.MINUTE, time[1])
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                Intent(context, ReminderReceiver::class.java).putExtra(ReminderReceiver.TASK_NAME, task.name),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
        }
        schedule(
            alarm,
            "daily_arc",
            dailyReminderTime,
            "Your daily arc is ready. Choose one quest and begin.",
            DAILY_REQUEST_CODE,
        )
    }

    private fun schedule(
        alarm: AlarmManager,
        id: String,
        timeValue: String,
        taskName: String,
        requestCode: Int,
    ) {
        val time = timeValue.split(":").map(String::toInt)
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time[0])
            set(Calendar.MINUTE, time[1])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, ReminderReceiver::class.java).putExtra(ReminderReceiver.TASK_NAME, taskName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
    }

    private fun createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily quests",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_quests"
        private const val DAILY_REQUEST_CODE = 1001
    }
}
