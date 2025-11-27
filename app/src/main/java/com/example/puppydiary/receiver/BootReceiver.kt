package com.example.puppydiary.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.puppydiary.data.local.PuppyDatabase
import com.example.puppydiary.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 부팅 완료 시 모든 예방접종 알람 재등록
            rescheduleAllAlarms(context)
        }
    }
    
    private fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = PuppyDatabase.getDatabase(context)
                val vaccinations = database.vaccinationDao().getAllVaccinations().first()
                
                vaccinations.forEach { vaccination ->
                    if (!vaccination.completed) {
                        AlarmScheduler.scheduleVaccinationAlarm(
                            context = context,
                            vaccineName = vaccination.vaccine,
                            nextDate = vaccination.nextDate,
                            notificationId = vaccination.id.toInt()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
