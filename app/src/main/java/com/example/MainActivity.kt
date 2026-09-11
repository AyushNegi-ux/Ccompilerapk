package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.DevBackground
import com.example.ui.theme.DevCardBorder
import com.example.ui.theme.DevPrimary
import com.example.ui.theme.DevSecondary
import com.example.ui.theme.DevSurface
import com.example.ui.theme.DevTextMuted
import com.example.ui.theme.DevTextSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                val isAppReady by viewModel.isAppReady.collectAsState()
                if (!isAppReady) {
                    LoadingScreen()
                } else {
                    CCompilerApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CCompilerApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val stats by viewModel.userStats.collectAsState()
    val compilerResult by viewModel.compilerResult.collectAsState()
    val streak = stats?.currentStreak ?: 1

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DevSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DevCardBorder)
            ) {
                // 1. Editor Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.EDITOR,
                    onClick = { viewModel.setTab(AppTab.EDITOR) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Editor",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Editor",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.EDITOR) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_editor")
                )

                // 2. Terminal Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.TERMINAL,
                    onClick = { viewModel.setTab(AppTab.TERMINAL) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (compilerResult != null) {
                                    Badge(
                                        containerColor = if (compilerResult!!.isSuccess) DevSecondary else Color(0xFFEF4444),
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Terminal",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.TERMINAL) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_terminal")
                )

                // 3. Practice Challenges Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.CHALLENGES,
                    onClick = { viewModel.setTab(AppTab.CHALLENGES) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Practice",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Practice",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.CHALLENGES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_practice")
                )

                // 4. Streaks & Progress Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.PROGRESS,
                    onClick = { viewModel.setTab(AppTab.PROGRESS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (streak > 0) {
                                    Badge(
                                        containerColor = Color(0xFFF97316),
                                        contentColor = Color.White
                                    ) {
                                        Text(text = "$streak", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streaks",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Streaks",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.PROGRESS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_streaks")
                )

                // 5. Files & Projects Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.FILES,
                    onClick = { viewModel.setTab(AppTab.FILES) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Projects",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Projects",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.FILES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_projects")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DevBackground)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.EDITOR -> EditorScreen(viewModel)
                AppTab.TERMINAL -> TerminalScreen(viewModel)
                AppTab.CHALLENGES -> ChallengesScreen(viewModel)
                AppTab.PROGRESS -> ProgressScreen(viewModel)
                AppTab.FILES -> ProjectsScreen(viewModel)
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = DevPrimary,
    selectedTextColor = DevPrimary,
    unselectedIconColor = DevTextMuted,
    unselectedTextColor = DevTextMuted,
    indicatorColor = DevPrimary.copy(alpha = 0.15f)
)
