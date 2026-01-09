package com.example.puppydiary.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.puppydiary.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "puppy_vaccination_channel"
        const val MEDICATION_CHANNEL_ID = "puppy_medication_channel"
        const val EXTRA_VACCINE_NAME = "vaccine_name"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_NEXT_DATE = "next_date"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_IS_MEDICATION = "is_medication"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isMedication = intent.getBooleanExtra(EXTRA_IS_MEDICATION, false)
        val nextDate = intent.getStringExtra(EXTRA_NEXT_DATE) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        if (isMedication) {
            val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "투약"
            createMedicationNotificationChannel(context)
            showMedicationNotification(context, medicationName, nextDate, notificationId)
        } else {
            val vaccineName = intent.getStringExtra(EXTRA_VACCINE_NAME) ?: "예방접종"
            createNotificationChannel(context)
            showNotification(context, vaccineName, nextDate, notificationId)
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "예방접종 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "강아지 예방접종 일정 알림"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(context: Context, vaccineName: String, nextDate: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🐕 예방접종 알림")
            .setContentText("$vaccineName 접종일이 3일 후($nextDate)입니다!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$vaccineName 접종 예정일이 3일 앞으로 다가왔습니다.\n접종일: $nextDate\n미리 병원 예약을 확인해주세요! 🏥"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createMedicationNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MEDICATION_CHANNEL_ID,
                "투약 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "반려동물 투약 일정 알림"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showMedicationNotification(context: Context, medicationName: String, nextDate: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, MEDICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("투약 알림")
            .setContentText("$medicationName 투약일입니다!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("오늘은 $medicationName 투약 예정일입니다.\n날짜: $nextDate\n잊지 말고 투약해주세요!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
