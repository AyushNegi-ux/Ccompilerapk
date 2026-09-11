package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.DevTextPrimary
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxDirective
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxOperator
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxType
import java.util.regex.Pattern

object SyntaxHighlighter {
    private val KEYWORDS = setOf(
        "return", "if", "else", "while", "for", "do", "break",
        "continue", "switch", "case", "default", "sizeof", "typedef", "const", "static"
    )

    private val TYPES = setOf(
        "int", "float", "char", "double", "void", "struct", "long", "short", "unsigned", "signed", "bool"
    )

    // Token regex pattern combining C syntactic elements
    private val PATTERN = Pattern.compile(
        "(?<COMMENT>//[^\n]*|/\\*[\\s\\S]*?\\*/)" +
        "|(?<DIRECTIVE>#[a-zA-Z_][a-zA-Z0-9_]*([^\n]*))" +
        "|(?<STRING>\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*')" +
        "|(?<NUMBER>\\b0[xX][0-9a-fA-F]+\\b|\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?[fF]?\\b)" +
        "|(?<WORD>\\b[a-zA-Z_][a-zA-Z0-9_]*\\b)" +
        "|(?<OPERATOR>[+\\-*/%&|^!=<>?:]+)"
    )

    fun highlight(code: String): AnnotatedString {
        val builder = AnnotatedString.Builder(code)

        val matcher = PATTERN.matcher(code)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            when {
                matcher.group("COMMENT") != null -> {
                    builder.addStyle(
                        SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic),
                        start, end
                    )
                }
                matcher.group("DIRECTIVE") != null -> {
                    builder.addStyle(
                        SpanStyle(color = SyntaxDirective, fontWeight = FontWeight.SemiBold),
                        start, end
                    )
                }
                matcher.group("STRING") != null -> {
                    builder.addStyle(
                        SpanStyle(color = SyntaxString),
                        start, end
                    )
                }
                matcher.group("NUMBER") != null -> {
                    builder.addStyle(
                        SpanStyle(color = SyntaxNumber),
                        start, end
                    )
                }
                matcher.group("WORD") != null -> {
                    val word = matcher.group("WORD")
                    when {
                        KEYWORDS.contains(word) -> {
                            builder.addStyle(
                                SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold),
                                start, end
                            )
                        }
                        TYPES.contains(word) -> {
                            builder.addStyle(
                                SpanStyle(color = SyntaxType, fontWeight = FontWeight.SemiBold),
                                start, end
                            )
                        }
                        // Check if followed by '(' -> function call or declaration
                        isFunctionCall(code, end) -> {
                            builder.addStyle(
                                SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.Medium),
                                start, end
                            )
                        }
                        else -> {
                            builder.addStyle(
                                SpanStyle(color = DevTextPrimary),
                                start, end
                            )
                        }
                    }
                }
                matcher.group("OPERATOR") != null -> {
                    builder.addStyle(
                        SpanStyle(color = SyntaxOperator),
                        start, end
                    )
                }
            }
        }

        return builder.toAnnotatedString()
    }

    private fun isFunctionCall(code: String, afterIndex: Int): Boolean {
        var i = afterIndex
        while (i < code.length && (code[i] == ' ' || code[i] == '\t')) {
            i++
        }
        return i < code.length && code[i] == '('
    }
}
