package com.example.puppydiary

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.puppydiary.data.local.SettingsDataStore
import com.example.puppydiary.ui.navigation.PuppyNavigation
import com.example.puppydiary.ui.theme.PuppyDiaryTheme
import com.example.puppydiary.utils.NotificationHelper
import com.example.puppydiary.viewmodel.PuppyViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    // 알림 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한 허용됨
            android.util.Log.d("PuppyDiary", "알림 권한 허용됨")
        } else {
            // 권한 거부됨
            android.util.Log.d("PuppyDiary", "알림 권한 거부됨")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 알림 채널 생성
        NotificationHelper.createNotificationChannel(this)
        
        // 알림 권한 요청 (Android 13 이상)
        requestNotificationPermission()
        
        // 정확한 알람 권한 확인 (Android 12 이상)
        checkExactAlarmPermission()

        val settingsDataStore = SettingsDataStore(this)

        setContent {
            val isDarkMode by settingsDataStore.isDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            PuppyDiaryTheme(darkTheme = isDarkMode) {
                val viewModel: PuppyViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PuppyNavigation(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = { enabled ->
                            scope.launch {
                                settingsDataStore.setDarkMode(enabled)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 이미 권한 있음
                    android.util.Log.d("PuppyDiary", "알림 권한 이미 허용됨")
                }
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // 정확한 알람 권한이 없으면 설정으로 이동 유도
                android.util.Log.d("PuppyDiary", "정확한 알람 권한 필요 - 설정에서 허용해주세요")
                // 필요시 아래 코드로 설정 화면으로 이동
                // val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                // startActivity(intent)
            } else {
                android.util.Log.d("PuppyDiary", "정확한 알람 권한 허용됨")
            }
        }
    }
}
