package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.compiler.CompilerDiagnostic
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.CodeEditor
import com.example.ui.theme.DevBackground
import com.example.ui.theme.DevCardBorder
import com.example.ui.theme.DevPrimary
import com.example.ui.theme.DevPrimaryDark
import com.example.ui.theme.DevSecondary
import com.example.ui.theme.DevSurface
import com.example.ui.theme.DevSurfaceVariant
import com.example.ui.theme.DevTextMuted
import com.example.ui.theme.DevTextPrimary
import com.example.ui.theme.DevTextSecondary

@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTitle by viewModel.currentProjectTitle.collectAsState()
    val editorCode by viewModel.editorCode.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()
    val compilerResult by viewModel.compilerResult.collectAsState()
    val currentChallenge by viewModel.currentChallengeWorkingOn.collectAsState()
    val diagnostics = compilerResult?.diagnostics ?: emptyList()

    var showHintDialog by remember { mutableStateOf(false) }
    var showSolutionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DevBackground)
    ) {
        // Editor Header Toolbar
        Surface(
            color = DevSurface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DevCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Filename indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(DevPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, DevPrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentTitle,
                            color = DevPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Save Button
                    IconButton(
                        onClick = {
                            viewModel.saveCurrentProject()
                            Toast.makeText(context, "Saved $currentTitle", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp).testTag("editor_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save File",
                            tint = DevTextSecondary
                        )
                    }

                    // Terminal Shortcut
                    IconButton(
                        onClick = { viewModel.setTab(AppTab.TERMINAL) },
                        modifier = Modifier.size(36.dp).testTag("editor_view_terminal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Terminal,
                            contentDescription = "View Terminal",
                            tint = DevTextSecondary
                        )
                    }

                    // Debug Run
                    OutlinedButton(
                        onClick = { viewModel.runCode(isDebugMode = true) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DevPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DevPrimary),
                        modifier = Modifier.height(36.dp).testTag("editor_debug_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Debug", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Run Button
                    Button(
                        onClick = { viewModel.runCode(isDebugMode = false) },
                        enabled = !isCompiling,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DevSecondary),
                        modifier = Modifier.height(36.dp).testTag("editor_run_button")
                    ) {
                        if (isCompiling) {
                            CircularProgressIndicator(
                                color = Color(0xFF0F172A),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Run",
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Practice Mode Toolbar Banner (Active when student opens a BCA practice challenge)
        if (currentChallenge != null) {
            val challenge = currentChallenge!!
            Surface(
                color = Color(0xFF1E2235),
                border = androidx.compose.foundation.BorderStroke(1.dp, DevPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎯 Practice: ${challenge.title}",
                                color = DevPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Do it yourself to learn! Need guidance?",
                            color = DevTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Hint Button
                        Surface(
                            onClick = { showHintDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF332A15),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Hint", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Show Solution Button (Gated behind confirmation so they try themselves first!)
                        Surface(
                            onClick = { showSolutionDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            color = DevSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DevCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = DevTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Solution", color = DevTextSecondary, fontSize = 11.sp)
                            }
                        }

                        // Dismiss banner
                        IconButton(
                            onClick = { viewModel.dismissCurrentChallengeBanner() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Practice Banner",
                                tint = DevTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Code Editor Canvas
        CodeEditor(
            value = editorCode,
            onValueChange = { viewModel.updateEditorCode(it) },
            diagnostics = diagnostics,
            modifier = Modifier.weight(1f)
        )
    }

    // Hint Dialog
    if (showHintDialog && currentChallenge != null) {
        AlertDialog(
            onDismissRequest = { showHintDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hint: ${currentChallenge!!.title}",
                        color = Color(0xFFFBBF24),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = currentChallenge!!.hints,
                        color = DevTextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Expected Output:\n${currentChallenge!!.expectedOutputs}",
                        color = Color(0xFF4ADE80),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHintDialog = false }) {
                    Text("Got It, Let Me Code!", color = DevPrimary)
                }
            },
            containerColor = DevSurface,
            tonalElevation = 6.dp
        )
    }

    // Show Solution Confirmation & Code Dialog
    if (showSolutionDialog && currentChallenge != null) {
        val challenge = currentChallenge!!
        var hasRevealedInDialog by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSolutionDialog = false },
            title = {
                Text(
                    text = if (hasRevealedInDialog) "Reference Solution" else "Reveal Solution Code?",
                    color = DevTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (!hasRevealedInDialog) {
                        Text(
                            text = "💡 Doing it yourself is the best way to master BCA C Programming!\n\nIf you are stuck and cannot solve it on your own, you can reveal the reference solution below.",
                            color = DevTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    } else {
                        Surface(
                            color = Color(0xFF0B0F17),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DevCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = challenge.solutionCode.ifEmpty { challenge.starterCode },
                                color = DevTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (!hasRevealedInDialog) {
                    Button(
                        onClick = { hasRevealedInDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DevPrimary)
                    ) {
                        Text("Show Code", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.loadSolutionIntoEditor(challenge)
                            showSolutionDialog = false
                            Toast.makeText(context, "Solution loaded into editor", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DevSecondary)
                    ) {
                        Text("Insert Solution in Editor", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSolutionDialog = false }) {
                    Text("Cancel", color = DevTextMuted)
                }
            },
            containerColor = DevSurface,
            tonalElevation = 6.dp
        )
    }
}
