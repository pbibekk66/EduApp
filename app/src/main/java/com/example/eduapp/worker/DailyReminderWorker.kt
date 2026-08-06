package com.example.eduapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.eduapp.MainActivity
import com.example.eduapp.R

/**
 * A background worker that handles Daily Practice Reminders.
 * WorkManager ensures this runs even if the app process is killed by the system.
 */
class DailyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    /**
     * The main execution point for the background task.
     * doWork() runs on a background thread provided by WorkManager.
     */
    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    /**
     * Builds and displays a system notification to the user.
     */
    private fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder_channel"

        // For Android 8.0 (Oreo) and above, notifications must be assigned to a Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Practice Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to practice math puzzles daily"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent that will be triggered when the user taps the notification.
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        // PendingIntent wraps the Intent, allowing the system to execute it on our app's behalf.
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Building the notification UI.
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setContentTitle("Time for NumNinja!")
            .setContentText("Don't forget to practice your math puzzles today to stay sharp!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismisses notification when tapped.
            .build()

        // Posting the notification.
        notificationManager.notify(1, notification)
    }
}
