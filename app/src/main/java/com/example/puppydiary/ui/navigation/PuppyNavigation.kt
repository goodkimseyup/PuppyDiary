package com.example.puppydiary.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.puppydiary.ui.components.BottomNavigation
import com.example.puppydiary.ui.components.BottomNavigationWithPager
import com.example.puppydiary.ui.screens.*
import com.example.puppydiary.viewmodel.PuppyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PuppyNavigation(
    viewModel: PuppyViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val puppyData by viewModel.puppyData.collectAsState()
    val allPuppies by viewModel.allPuppies.collectAsState()
    val scope = rememberCoroutineScope()

    // 강아지가 등록되어 있는지 확인
    val hasPuppy = allPuppies.isNotEmpty()

    if (!hasPuppy) {
        // 등록 화면
        RegisterPuppyScreen(
            viewModel = viewModel,
            onRegistrationComplete = { }
        )
    } else {
        // 메인 화면 (HorizontalPager로 스와이프 지원)
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { 4 } // 홈, 통계, 일기, 설정
        )
        
        // 사진첩 표시 상태
        var showGallery by remember { mutableStateOf(false) }
        // AI 챗봇 표시 상태
        var showAIChat by remember { mutableStateOf(false) }
        // 건강 리포트 표시 상태
        var showHealthReport by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                if (!showGallery && !showAIChat && !showHealthReport) {
                    BottomNavigationWithPager(
                        pagerState = pagerState,
                        onPageSelected = { page ->
                            scope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            when {
                showGallery -> {
                    // 사진첩 전체 화면
                    PhotoGalleryScreen(
                        viewModel = viewModel,
                        onBack = { showGallery = false }
                    )
                }
                showAIChat -> {
                    // AI 챗봇 화면
                    AIChatScreen(
                        onNavigateBack = { showAIChat = false }
                    )
                }
                showHealthReport -> {
                    // 건강 리포트 화면
                    HealthReportScreen(
                        viewModel = viewModel,
                        onNavigateBack = { showHealthReport = false }
                    )
                }
                else -> {
                    // 메인 Pager (스와이프 가능)
                    HorizontalPager(
                        state = pagerState,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToGallery = { showGallery = true },
                                onNavigateToAIChat = { showAIChat = true },
                                onNavigateToHealthReport = { showHealthReport = true }
                            )
                            1 -> StatsScreen(viewModel = viewModel)
                            2 -> DiaryScreen(viewModel = viewModel)
                            3 -> SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = onDarkModeChange
                            )
                        }
                    }
                }
            }
        }
    }
}
