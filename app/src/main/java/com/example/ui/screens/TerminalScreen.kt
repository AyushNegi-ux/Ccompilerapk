package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.components.TerminalEmulator

@Composable
fun TerminalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val compilerResult by viewModel.compilerResult.collectAsState()
    val stdinInput by viewModel.stdinInput.collectAsState()

    TerminalEmulator(
        result = compilerResult,
        stdinValue = stdinInput,
        onStdinChange = { viewModel.updateStdinInput(it) },
        onRerun = { stdin ->
            viewModel.updateStdinInput(stdin)
            viewModel.runCode(isDebugMode = false)
        },
        onClear = { viewModel.clearTerminal() },
        modifier = modifier.fillMaxSize()
    )
}
