package com.example.puppydiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.puppydiary.ui.navigation.PuppyNavigation
import com.example.puppydiary.ui.theme.PuppyDiaryTheme
import com.example.puppydiary.utils.NotificationHelper
import com.example.puppydiary.viewmodel.PuppyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 알림 채널 생성
        NotificationHelper.createNotificationChannel(this)

        setContent {
            PuppyDiaryTheme {
                val viewModel: PuppyViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PuppyNavigation(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
