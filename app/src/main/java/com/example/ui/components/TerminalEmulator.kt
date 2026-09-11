package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.CompilerResult
import com.example.ui.theme.DevCardBorder
import com.example.ui.theme.DevPrimary
import com.example.ui.theme.DevSecondary
import com.example.ui.theme.DevSurface
import com.example.ui.theme.DevSurfaceVariant
import com.example.ui.theme.DevTextMuted
import com.example.ui.theme.DevTextPrimary
import com.example.ui.theme.DevTextSecondary
import com.example.ui.theme.TermBackground
import com.example.ui.theme.TermError
import com.example.ui.theme.TermForeground
import com.example.ui.theme.TermPrompt
import com.example.ui.theme.TermSuccess

@Composable
fun TerminalEmulator(
    result: CompilerResult?,
    stdinValue: String,
    onStdinChange: (String) -> Unit,
    onRerun: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDebugPane by remember { mutableStateOf(false) }
    var debugStepIndex by remember { mutableIntStateOf(0) }
    val outputScroll = rememberScrollState()

    val snapshots = result?.debugSnapshots ?: emptyList()
    val activeSnapshot = if (snapshots.isNotEmpty() && debugStepIndex < snapshots.size) {
        snapshots[debugStepIndex]
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TermBackground)
    ) {
        // Terminal Window Header Bar
        Surface(
            color = DevSurface,
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
                // macOS/Linux window dots
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "terminal • gcc main.c",
                        color = DevTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Header Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (snapshots.isNotEmpty()) {
                        IconButton(
                            onClick = { showDebugPane = !showDebugPane },
                            modifier = Modifier.size(32.dp).testTag("terminal_debug_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Debug Inspection",
                                tint = if (showDebugPane) DevPrimary else DevTextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val textToCopy = buildString {
                                if (result != null) {
                                    append(result.output)
                                    if (result.errorOutput.isNotEmpty()) append("\n").append(result.errorOutput)
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Output", textToCopy))
                            Toast.makeText(context, "Terminal output copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("terminal_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Output",
                            tint = DevTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp).testTag("terminal_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Terminal",
                            tint = DevTextSecondary
                        )
                    }
                }
            }
        }

        // Metrics & Status Banner
        if (result != null) {
            Surface(
                color = if (result.isSuccess) Color(0xFF0F291E) else Color(0xFF2E1515),
                modifier = Modifier.fillMaxWidth().border(1.dp, if (result.isSuccess) Color(0xFF10B981) else Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (result.isSuccess) TermSuccess else TermError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (result.isSuccess) "Process finished with exit code ${result.exitCode}" else "Compilation / Execution Failed",
                            color = if (result.isSuccess) TermSuccess else TermError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${result.metrics.executionTimeMs}ms • ${result.metrics.memoryAllocatedBytes}B mem",
                        color = DevTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Main Terminal Output Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(outputScroll)
        ) {
            Column {
                // Command invocation prompt
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$ ",
                        color = TermPrompt,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "./a.out",
                        color = TermForeground,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (result == null) {
                    Text(
                        text = "// Tap 'Run' to compile and execute your C program.\n// Standard output and error streams will appear here.",
                        color = DevTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                } else {
                    // Standard Output
                    if (result.output.isNotEmpty()) {
                        Text(
                            text = result.output,
                            color = TermForeground,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.testTag("terminal_stdout_text")
                        )
                    }

                    // Error Output
                    if (result.errorOutput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.errorOutput,
                            color = TermError,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.testTag("terminal_stderr_text")
                        )
                    }
                }
            }
        }

        // Debug Inspector Pane (if toggled and available)
        if (showDebugPane && activeSnapshot != null) {
            Surface(
                color = DevSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DevCardBorder)
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DEBUGGER: Step ${activeSnapshot.stepIndex}/${snapshots.size} (Line ${activeSnapshot.line})",
                            color = DevPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = activeSnapshot.description,
                            color = DevTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = debugStepIndex.toFloat(),
                        onValueChange = { debugStepIndex = it.toInt() },
                        valueRange = 0f..(snapshots.size - 1).toFloat(),
                        steps = maxOf(0, snapshots.size - 2),
                        colors = SliderDefaults.colors(
                            thumbColor = DevPrimary,
                            activeTrackColor = DevPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Variables table
                    if (activeSnapshot.variables.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (v in activeSnapshot.variables) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DevSurfaceVariant),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.border(1.dp, DevCardBorder, RoundedCornerShape(6.dp))
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text(
                                            text = "${v.name} (${v.type})",
                                            color = DevPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "= ${v.value}",
                                            color = DevTextPrimary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "@${v.memoryAddress}",
                                            color = DevTextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stdin Input Bar (for scanf / interactive input)
        Surface(
            color = DevSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DevCardBorder)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = stdinValue,
                    onValueChange = onStdinChange,
                    placeholder = {
                        Text(
                            text = "stdin input (e.g. for scanf)...",
                            color = DevTextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = DevTextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DevPrimary,
                        unfocusedBorderColor = DevCardBorder,
                        cursorColor = DevPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("terminal_stdin_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onRerun(stdinValue) },
                    colors = ButtonDefaults.buttonColors(containerColor = DevPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp).testTag("terminal_send_stdin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Run",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
