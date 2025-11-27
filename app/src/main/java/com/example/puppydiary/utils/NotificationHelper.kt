package com.example.puppydiary.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.puppydiary.MainActivity
import com.example.puppydiary.data.model.VaccinationReminder

object NotificationHelper {
    private const val CHANNEL_ID = "puppy_vaccination_channel"
    private const val CHANNEL_NAME = "예방접종 알림"
    private const val CHANNEL_DESCRIPTION = "강아지 예방접종 일정 알림"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleVaccinationReminder(context: Context, reminder: VaccinationReminder) {
        showVaccinationNotification(context, reminder)
    }

    private fun showVaccinationNotification(context: Context, reminder: VaccinationReminder) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 🔴 시스템 아이콘 사용
            .setContentTitle("🐕 예방접종 알림")
            .setContentText("${reminder.vaccineName} 접종 예정일이 다가왔습니다")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(reminder.notificationId, builder.build())
            } catch (e: SecurityException) {
                // 알림 권한이 없는 경우 처리
            }
        }
    }

    fun cancelVaccinationReminder(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}