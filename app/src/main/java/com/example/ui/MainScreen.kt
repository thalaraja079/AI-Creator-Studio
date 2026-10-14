package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FixedBottomBannerAd
import com.example.ui.components.RewardedAdDialog
import com.example.ui.screens.HistoryTab
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.SettingsTab
import com.example.ui.screens.ToolsTab

@Composable
fun MainScreen(viewModel: CreatorViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept back button to navigate to Home tab if on another tab
    if (state.currentTab != CreatorTab.HOME) {
        BackHandler {
            viewModel.switchTab(CreatorTab.HOME)
        }
    }

    // Display error messages via Snackbar
    LaunchedEffect(state.errorMessage) {
        val error = state.errorMessage
        if (error != null) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }

    // Rewarded Ad Dialog overlay
    if (state.showRewardedAdDialog) {
        RewardedAdDialog(
            progress = state.adProgress,
            onDismiss = { viewModel.dismissAdDialog() }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                // Fixed Bottom Banner Ad anchored persistently directly above the Navigation Bar
                FixedBottomBannerAd()

                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    CreatorTab.values().forEach { tab ->
                        val isSelected = state.currentTab == tab
                        val icon = when (tab) {
                            CreatorTab.HOME -> Icons.Default.Home
                            CreatorTab.TOOLS -> Icons.Default.AutoAwesome
                            CreatorTab.HISTORY -> Icons.Default.History
                            CreatorTab.SETTINGS -> Icons.Default.Settings
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.switchTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentTab) {
                CreatorTab.HOME -> HomeDashboardScreen(state = state, viewModel = viewModel)
                CreatorTab.TOOLS -> ToolsTab(state = state, viewModel = viewModel)
                CreatorTab.HISTORY -> HistoryTab(state = state, viewModel = viewModel)
                CreatorTab.SETTINGS -> SettingsTab(state = state, viewModel = viewModel)
            }
        }
    }
}
