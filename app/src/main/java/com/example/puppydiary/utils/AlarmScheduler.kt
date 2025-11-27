package com.example.puppydiary.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.puppydiary.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

object AlarmScheduler {
    
    private const val TAG = "AlarmScheduler"
    
    /**
     * 예방접종 3일 전 알람 예약
     */
    fun scheduleVaccinationAlarm(
        context: Context,
        vaccineName: String,
        nextDate: String,
        notificationId: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // 접종일 파싱
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val vaccineDate = try {
            dateFormat.parse(nextDate)
        } catch (e: Exception) {
            Log.e(TAG, "날짜 파싱 실패: $nextDate", e)
            return
        }
        
        if (vaccineDate == null) {
            Log.e(TAG, "날짜가 null입니다: $nextDate")
            return
        }
        
        // 3일 전 알람 시간 계산 (오전 9시)
        val calendar = Calendar.getInstance().apply {
            time = vaccineDate
            add(Calendar.DAY_OF_MONTH, -3) // 3일 전
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val alarmTime = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()
        
        // 이미 지난 시간이면 알람 설정하지 않음
        if (alarmTime <= currentTime) {
            Log.d(TAG, "알람 시간이 이미 지났습니다: $vaccineName")
            return
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_VACCINE_NAME, vaccineName)
            putExtra(AlarmReceiver.EXTRA_NEXT_DATE, nextDate)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        try {
            // Android 12 이상에서는 SCHEDULE_EXACT_ALARM 권한 필요
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                    )
                    Log.d(TAG, "정확한 알람 예약 완료: $vaccineName, 시간: ${dateFormat.format(Date(alarmTime))}")
                } else {
                    // 정확한 알람 권한이 없으면 일반 알람 사용
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                    )
                    Log.d(TAG, "일반 알람 예약 완료: $vaccineName")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
                Log.d(TAG, "알람 예약 완료: $vaccineName")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
                Log.d(TAG, "알람 예약 완료 (구버전): $vaccineName")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "알람 예약 권한 오류", e)
        }
    }
    
    /**
     * 예약된 알람 취소
     */
    fun cancelVaccinationAlarm(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "알람 취소 완료: $notificationId")
        }
    }
}
