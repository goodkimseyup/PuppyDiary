package com.example.puppydiary.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 2024 트렌디한 파스텔 & 소프트 컬러 팔레트
object AppColors {
    // Primary - 소프트 코랄 핑크
    val Primary = Color(0xFFFF6B9D)
    val PrimaryLight = Color(0xFFFFB5C5)
    val PrimaryDark = Color(0xFFE91E63)
    
    // Secondary - 소프트 퍼플
    val Secondary = Color(0xFFA855F7)
    val SecondaryLight = Color(0xFFD8B4FE)
    val SecondaryDark = Color(0xFF7C3AED)
    
    // Accent - 민트/틸
    val Accent = Color(0xFF2DD4BF)
    val AccentLight = Color(0xFF99F6E4)
    
    // Warm - 피치/오렌지
    val Warm = Color(0xFFFB923C)
    val WarmLight = Color(0xFFFED7AA)
    
    // Cool - 스카이 블루
    val Cool = Color(0xFF38BDF8)
    val CoolLight = Color(0xFFBAE6FD)
    
    // Neutral
    val Surface = Color(0xFFFAFAFC)
    val SurfaceVariant = Color(0xFFF1F5F9)
    val Background = Color(0xFFF8FAFC)
    
    // Dark Mode
    val DarkSurface = Color(0xFF1E1E2E)
    val DarkSurfaceVariant = Color(0xFF2A2A3E)
    val DarkBackground = Color(0xFF11111B)
    
    // Gradient Colors
    val GradientStart = Color(0xFFFF6B9D)
    val GradientEnd = Color(0xFFA855F7)
    
    // Card Colors
    val CardLight = Color(0xFFFFFFFF)
    val CardDark = Color(0xFF2A2A3E)
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.PrimaryDark,
    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = AppColors.SecondaryLight,
    onSecondaryContainer = AppColors.SecondaryDark,
    tertiary = AppColors.Accent,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.AccentLight,
    onTertiaryContainer = Color(0xFF0D9488),
    background = AppColors.Background,
    onBackground = Color(0xFF1E293B),
    surface = AppColors.Surface,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryLight,
    onPrimary = AppColors.PrimaryDark,
    primaryContainer = AppColors.PrimaryDark,
    onPrimaryContainer = AppColors.PrimaryLight,
    secondary = AppColors.SecondaryLight,
    onSecondary = AppColors.SecondaryDark,
    secondaryContainer = AppColors.SecondaryDark,
    onSecondaryContainer = AppColors.SecondaryLight,
    tertiary = AppColors.AccentLight,
    onTertiary = Color(0xFF0D9488),
    tertiaryContainer = Color(0xFF0D9488),
    onTertiaryContainer = AppColors.AccentLight,
    background = AppColors.DarkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = AppColors.DarkSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun PuppyDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 커스텀 색상 사용
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) {
                AppColors.DarkBackground.toArgb()
            } else {
                AppColors.Background.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
