package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.DevBackground
import com.example.ui.theme.DevCardBorder
import com.example.ui.theme.DevPrimary
import com.example.ui.theme.DevSecondary
import com.example.ui.theme.DevSurface
import com.example.ui.theme.DevSurfaceVariant
import com.example.ui.theme.DevTextMuted
import com.example.ui.theme.DevTextPrimary
import com.example.ui.theme.DevTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.userStats.collectAsState()
    val activities by viewModel.recentActivities.collectAsState()

    val streak = stats?.currentStreak ?: 1
    val longestStreak = stats?.longestStreak ?: 1
    val totalXp = stats?.totalXp ?: 0
    val totalRuns = stats?.totalRuns ?: 0
    val totalSolved = stats?.totalSolved ?: 0

    // Compute Level
    val level = (totalXp / 100) + 1
    val currentLevelXp = totalXp % 100
    val rankTitle = when {
        level <= 1 -> "C Syntax Explorer"
        level <= 2 -> "Logic Builder"
        level <= 3 -> "Loop Master"
        level <= 4 -> "Function Artisan"
        level <= 5 -> "Pointer Alchemist"
        else -> "C Architecture Pro"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DevBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streak Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DevSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF97316).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("streak_hero_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF97316).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak Flame",
                                tint = Color(0xFFFB923C),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "$streak DAY STREAK",
                                color = Color(0xFFFB923C),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Keep coding daily to build fluency!",
                                color = DevTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(DevSurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, DevCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Best: $longestStreak d",
                            color = DevTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 14-Day Activity Heatmap
                Text(
                    text = "Activity Heatmap (Past 14 Days)",
                    color = DevTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val activityMap = activities.associateBy { it.dateString }
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val dayFormat = SimpleDateFormat("E", Locale.US)

                val daysList = (13 downTo 0).map { offset ->
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DATE, -offset)
                    val dStr = dateFormat.format(cal.time)
                    val label = dayFormat.format(cal.time).take(1)
                    val hasActivity = (activityMap[dStr]?.runCount ?: 0) > 0 || (activityMap[dStr]?.challengesCompleted ?: 0) > 0 || offset == 0
                    Triple(dStr, label, hasActivity)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for ((_, label, active) in daysList) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (active) Color(0xFF10B981) else DevSurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (active) Color(0xFF34D399) else DevCardBorder,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                color = DevTextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Level & XP Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DevSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DevCardBorder, RoundedCornerShape(14.dp))
                .testTag("level_progress_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Level $level: $rankTitle",
                            color = DevPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalXp Total XP Earned",
                            color = DevTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(DevPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$currentLevelXp / 100 XP",
                            color = DevPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { currentLevelXp / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = DevPrimary,
                    trackColor = DevSurfaceVariant
                )
            }
        }

        // Lifetime Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Programs run
            Card(
                colors = CardDefaults.cardColors(containerColor = DevSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DevCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = DevPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$totalRuns", color = DevTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Code Runs", color = DevTextMuted, fontSize = 11.sp)
                }
            }

            // Challenges Solved
            Card(
                colors = CardDefaults.cardColors(containerColor = DevSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DevCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = DevSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$totalSolved", color = DevTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Challenges", color = DevTextMuted, fontSize = 11.sp)
                }
            }

            // Total XP
            Card(
                colors = CardDefaults.cardColors(containerColor = DevSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DevCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$totalXp", color = DevTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Total XP", color = DevTextMuted, fontSize = 11.sp)
                }
            }
        }

        // C Quick Reference Guide
        Card(
            colors = CardDefaults.cardColors(containerColor = DevSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DevCardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "C Language Quick Reference",
                    color = DevTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                ReferenceRow(specifier = "%d, %i", description = "Signed integer")
                ReferenceRow(specifier = "%f, %.2f", description = "Floating point number")
                ReferenceRow(specifier = "%c", description = "Single character")
                ReferenceRow(specifier = "%s", description = "Null-terminated string")
                ReferenceRow(specifier = "%p", description = "Pointer memory address")
                ReferenceRow(specifier = "&var", description = "Address-of operator")
                ReferenceRow(specifier = "*ptr", description = "Pointer dereference operator")
            }
        }
    }
}

@Composable
fun ReferenceRow(specifier: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = specifier,
            color = DevPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            color = DevTextSecondary,
            fontSize = 12.sp
        )
    }
}
