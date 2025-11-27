package com.example.puppydiary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.puppydiary.ui.theme.AppColors

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        NavigationItem("home", "홈", Icons.Filled.Home, Icons.Default.Home),
        NavigationItem("stats", "통계", Icons.Filled.Info, Icons.Default.Info),
        NavigationItem("diary", "일기", Icons.Filled.Create, Icons.Default.Create),
        NavigationItem("gallery", "사진첩", Icons.Filled.Favorite, Icons.Default.Favorite),
        NavigationItem("settings", "설정", Icons.Filled.Settings, Icons.Default.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(80.dp)
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (isSelected) item.selectedIcon else item.unselectedIcon, 
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            item.title,
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.Primary,
                        selectedTextColor = AppColors.Primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = AppColors.Primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
