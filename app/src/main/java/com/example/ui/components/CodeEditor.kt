package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.CompilerDiagnostic
import com.example.ui.theme.DevBackground
import com.example.ui.theme.DevCardBorder
import com.example.ui.theme.DevPrimary
import com.example.ui.theme.DevSurface
import com.example.ui.theme.DevSurfaceVariant
import com.example.ui.theme.DevTextMuted
import com.example.ui.theme.DevTextPrimary
import com.example.ui.theme.DevTextSecondary
import com.example.ui.theme.TermError

@Composable
fun CodeEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    diagnostics: List<CompilerDiagnostic> = emptyList(),
    fontSizeSp: Int = 14
) {
    val quickSymbols = listOf(
        "{", "}", "(", ")", "[", "]", ";", "\"", "'",
        "<", ">", "=", "+", "-", "*", "/", "&", "%", "!", "|",
        "TAB", "//", "#include <stdio.h>", "printf(\"\");"
    )

    // Undo / Redo history
    val history = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }
    val focusRequester = remember { FocusRequester() }

    fun pushToHistory(current: TextFieldValue) {
        val last = history.lastOrNull()
        if (last == null || (last.text != current.text && (kotlin.math.abs(last.text.length - current.text.length) > 1 || current.text.endsWith(" ") || current.text.endsWith("\n") || current.text.endsWith(";")))) {
            if (history.size > 50) history.removeAt(0)
            history.add(current)
            redoStack.clear()
        }
    }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val quickBarScroll = rememberScrollState()

    val lineCount = remember(value.text) {
        maxOf(1, value.text.count { it == '\n' } + 1)
    }

    val errorLines = remember(diagnostics) {
        diagnostics.filter { it.isError }.map { it.line }.toSet()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DevBackground)
    ) {
        // Quick Symbols Accessory Toolbar
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
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo
                IconButton(
                    onClick = {
                        if (history.isNotEmpty()) {
                            val previous = history.removeAt(history.lastIndex)
                            redoStack.add(value)
                            onValueChange(previous)
                        }
                    },
                    enabled = history.isNotEmpty(),
                    modifier = Modifier.width(36.dp).height(36.dp).testTag("editor_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (history.isNotEmpty()) DevPrimary else DevTextMuted
                    )
                }

                // Redo
                IconButton(
                    onClick = {
                        if (redoStack.isNotEmpty()) {
                            val next = redoStack.removeAt(redoStack.lastIndex)
                            history.add(value)
                            onValueChange(next)
                        }
                    },
                    enabled = redoStack.isNotEmpty(),
                    modifier = Modifier.width(36.dp).height(36.dp).testTag("editor_redo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (redoStack.isNotEmpty()) DevPrimary else DevTextMuted
                    )
                }

                // Format Code
                IconButton(
                    onClick = {
                        pushToHistory(value)
                        val formatted = formatCCode(value.text)
                        onValueChange(value.copy(text = formatted, selection = TextRange(formatted.length)))
                    },
                    modifier = Modifier.width(36.dp).height(36.dp).testTag("editor_format_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Format Code",
                        tint = DevPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Symbol Buttons
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(quickBarScroll)
                ) {
                    for (symbol in quickSymbols) {
                        Surface(
                            onClick = {
                                pushToHistory(value)
                                val toInsert = when (symbol) {
                                    "TAB" -> "    "
                                    "printf(\"\");" -> "printf(\"\");"
                                    "#include <stdio.h>" -> "#include <stdio.h>\n"
                                    else -> symbol
                                }

                                val cursor = value.selection.start
                                val newText = value.text.substring(0, cursor) + toInsert + value.text.substring(value.selection.end)
                                val newCursor = if (symbol == "printf(\"\");") cursor + 9 else cursor + toInsert.length
                                onValueChange(
                                    value.copy(
                                        text = newText,
                                        selection = TextRange(newCursor)
                                    )
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = DevSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DevCardBorder),
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .testTag("quick_symbol_$symbol")
                        ) {
                            Text(
                                text = symbol,
                                color = DevTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Editor Canvas with Line Numbers
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(verticalScroll)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .background(DevSurface)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .width(IntrinsicSize.Min)
            ) {
                for (i in 1..lineCount) {
                    val hasError = errorLines.contains(i)
                    Text(
                        text = "$i",
                        color = if (hasError) TermError else DevTextMuted,
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        fontWeight = if (hasError) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = (fontSizeSp + 6).sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Separator border
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .background(DevCardBorder)
            )

            // Code Text Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { newVal ->
                        // Detect enter key for auto-indent
                        if (newVal.text.length == value.text.length + 1 &&
                            newVal.selection.start > 0 &&
                            newVal.text[newVal.selection.start - 1] == '\n'
                        ) {
                            pushToHistory(value)
                            val indent = calculateIndent(value.text, newVal.selection.start - 1)
                            if (indent.isNotEmpty()) {
                                val curPos = newVal.selection.start
                                val indentedText = newVal.text.substring(0, curPos) + indent + newVal.text.substring(curPos)
                                onValueChange(
                                    newVal.copy(
                                        text = indentedText,
                                        selection = TextRange(curPos + indent.length)
                                    )
                                )
                                return@BasicTextField
                            }
                        }

                        pushToHistory(value)
                        onValueChange(newVal)
                    },
                    textStyle = TextStyle(
                        color = DevTextPrimary,
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = (fontSizeSp + 6).sp
                    ),
                    cursorBrush = SolidColor(DevPrimary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii
                    ),
                    visualTransformation = VisualTransformation { text ->
                        TransformedText(
                            text = SyntaxHighlighter.highlight(text.text),
                            offsetMapping = OffsetMapping.Identity
                        )
                    },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .defaultMinSize(minWidth = 500.dp, minHeight = 400.dp)
                        .testTag("code_editor_input")
                )
            }
        }
    }
}

private fun calculateIndent(text: String, newlinePos: Int): String {
    // Look at the previous line
    var lineStart = text.lastIndexOf('\n', newlinePos - 1)
    lineStart = if (lineStart == -1) 0 else lineStart + 1
    val prevLine = text.substring(lineStart, newlinePos)
    val leadingSpaces = prevLine.takeWhile { it == ' ' || it == '\t' }
    val trimmed = prevLine.trim()
    val extra = if (trimmed.endsWith('{')) "    " else ""
    return leadingSpaces + extra
}

fun formatCCode(code: String): String {
    val lines = code.lines()
    val formatted = StringBuilder()
    var indentLevel = 0

    for (rawLine in lines) {
        val line = rawLine.trim()
        if (line.isEmpty()) {
            formatted.append('\n')
            continue
        }

        // Adjust indent for closing brace
        val closingCount = line.count { it == '}' }
        val openingCount = line.count { it == '{' }

        if (line.startsWith('}')) {
            indentLevel = maxOf(0, indentLevel - 1)
        }

        val indent = "    ".repeat(indentLevel)
        formatted.append(indent).append(line).append('\n')

        if (!line.startsWith('}')) {
            indentLevel = maxOf(0, indentLevel + openingCount - closingCount)
        }
    }

    return formatted.toString().trimEnd() + "\n"
}
