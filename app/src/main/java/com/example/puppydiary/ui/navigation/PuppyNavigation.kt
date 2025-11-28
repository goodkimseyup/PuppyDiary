package com.example.puppydiary.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.puppydiary.ui.components.BottomNavigationWithPager
import com.example.puppydiary.ui.screens.*
import com.example.puppydiary.viewmodel.PuppyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PuppyNavigation(
    viewModel: PuppyViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val puppyData by viewModel.puppyData.collectAsState()
    val allPuppies by viewModel.allPuppies.collectAsState()
    val scope = rememberCoroutineScope()

    // 초기 로딩 상태
    var isLoading by remember { mutableStateOf(true) }

    // 사진첩 표시 상태
    var showGallery by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // 강아지가 등록되어 있는지 확인
    val hasPuppy = allPuppies.isNotEmpty()

    if (!hasPuppy) {
        // 등록 화면
        RegisterPuppyScreen(
            viewModel = viewModel,
            onRegistrationComplete = { }
        )
    } else {
        // 메인 화면 (Pager)
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { 4 } // 홈, 통계, 일기, 설정
        )

        Scaffold(
            bottomBar = {
                BottomNavigationWithPager(
                    pagerState = pagerState,
                    onPageSelected = { page ->
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                )
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToGallery = { showGallery = true }
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

        // 사진첩 전체 화면 다이얼로그
        if (showGallery) {
            Dialog(
                onDismissRequest = { showGallery = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhotoGalleryScreen(
                        viewModel = viewModel,
                        onBack = { showGallery = false }
                    )
                }
            }
        }
    }
}
