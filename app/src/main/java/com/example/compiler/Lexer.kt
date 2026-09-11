package com.example.compiler

class Lexer(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1
    private var column = 1
    private var tokenStartColumn = 1

    private val keywords = mapOf(
        "int" to TokenType.KEYWORD_INT,
        "float" to TokenType.KEYWORD_FLOAT,
        "char" to TokenType.KEYWORD_CHAR,
        "double" to TokenType.KEYWORD_DOUBLE,
        "void" to TokenType.KEYWORD_VOID,
        "return" to TokenType.KEYWORD_RETURN,
        "if" to TokenType.KEYWORD_IF,
        "else" to TokenType.KEYWORD_ELSE,
        "while" to TokenType.KEYWORD_WHILE,
        "for" to TokenType.KEYWORD_FOR,
        "do" to TokenType.KEYWORD_DO,
        "break" to TokenType.KEYWORD_BREAK,
        "continue" to TokenType.KEYWORD_CONTINUE,
        "switch" to TokenType.KEYWORD_SWITCH,
        "case" to TokenType.KEYWORD_CASE,
        "default" to TokenType.KEYWORD_DEFAULT,
        "struct" to TokenType.KEYWORD_STRUCT,
        "sizeof" to TokenType.KEYWORD_SIZEOF,
        "typedef" to TokenType.KEYWORD_TYPEDEF,
        "const" to TokenType.KEYWORD_CONST,
        "static" to TokenType.KEYWORD_STATIC
    )

    fun tokenize(): List<Token> {
        while (!isAtEnd()) {
            start = current
            tokenStartColumn = column
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line, column))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LPAREN)
            ')' -> addToken(TokenType.RPAREN)
            '{' -> addToken(TokenType.LBRACE)
            '}' -> addToken(TokenType.RBRACE)
            '[' -> addToken(TokenType.LBRACKET)
            ']' -> addToken(TokenType.RBRACKET)
            ';' -> addToken(TokenType.SEMICOLON)
            ',' -> addToken(TokenType.COMMA)
            ':' -> addToken(TokenType.COLON)
            '?' -> addToken(TokenType.QUESTION)
            '~' -> addToken(TokenType.TILDE)
            '^' -> addToken(TokenType.CARET)
            '.' -> addToken(TokenType.DOT)

            '+' -> {
                when {
                    match('+') -> addToken(TokenType.PLUS_PLUS)
                    match('=') -> addToken(TokenType.PLUS_ASSIGN)
                    else -> addToken(TokenType.PLUS)
                }
            }
            '-' -> {
                when {
                    match('-') -> addToken(TokenType.MINUS_MINUS)
                    match('=') -> addToken(TokenType.MINUS_ASSIGN)
                    match('>') -> addToken(TokenType.ARROW)
                    else -> addToken(TokenType.MINUS)
                }
            }
            '*' -> {
                if (match('=')) addToken(TokenType.STAR_ASSIGN)
                else addToken(TokenType.STAR)
            }
            '/' -> {
                if (match('/')) {
                    // Single line comment
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else if (match('*')) {
                    // Multi line comment
                    blockComment()
                } else if (match('=')) {
                    addToken(TokenType.SLASH_ASSIGN)
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            '%' -> {
                if (match('=')) addToken(TokenType.PERCENT_ASSIGN)
                else addToken(TokenType.PERCENT)
            }
            '=' -> {
                if (match('=')) addToken(TokenType.EQUAL_EQUAL)
                else addToken(TokenType.ASSIGN)
            }
            '!' -> {
                if (match('=')) addToken(TokenType.BANG_EQUAL)
                else addToken(TokenType.BANG)
            }
            '<' -> {
                when {
                    match('=') -> addToken(TokenType.LESS_EQUAL)
                    match('<') -> addToken(TokenType.SHL)
                    else -> addToken(TokenType.LESS)
                }
            }
            '>' -> {
                when {
                    match('=') -> addToken(TokenType.GREATER_EQUAL)
                    match('>') -> addToken(TokenType.SHR)
                    else -> addToken(TokenType.GREATER)
                }
            }
            '&' -> {
                if (match('&')) addToken(TokenType.LOGICAL_AND)
                else addToken(TokenType.AMPERSAND)
            }
            '|' -> {
                if (match('|')) addToken(TokenType.LOGICAL_OR)
                else addToken(TokenType.PIPE)
            }
            '#' -> {
                preprocessorDirective()
            }
            ' ', '\r', '\t' -> {
                // Ignore whitespace
            }
            '\n' -> {
                line++
                column = 1
            }
            '"' -> stringLiteral()
            '\'' -> charLiteral()
            else -> {
                if (isDigit(c)) {
                    numberLiteral(c)
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    // Unknown character, skip gracefully
                }
            }
        }
    }

    private fun blockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance() // *
                advance() // /
                return
            }
            if (peek() == '\n') {
                line++
                column = 0
            }
            advance()
        }
    }

    private fun preprocessorDirective() {
        while (peek() != '\n' && !isAtEnd()) {
            advance()
        }
        val text = source.substring(start, current).trim()
        addToken(TokenType.PREPROCESSOR, text)
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun numberLiteral(firstChar: Char) {
        if (firstChar == '0' && (peek() == 'x' || peek() == 'X')) {
            advance() // x
            while (isHexDigit(peek())) advance()
            val text = source.substring(start, current)
            val value = try {
                java.lang.Long.parseLong(text.substring(2), 16).toInt()
            } catch (e: Exception) {
                0
            }
            addToken(TokenType.INT_LITERAL, value)
            return
        }

        while (isDigit(peek())) advance()

        var isFloat = false
        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true
            advance() // Consume '.'
            while (isDigit(peek())) advance()
        }

        if (peek() == 'f' || peek() == 'F') {
            isFloat = true
            advance()
        }

        val text = source.substring(start, current).trimEnd('f', 'F')
        if (isFloat) {
            val value = text.toDoubleOrNull() ?: 0.0
            addToken(TokenType.FLOAT_LITERAL, value)
        } else {
            val value = text.toIntOrNull() ?: 0
            addToken(TokenType.INT_LITERAL, value)
        }
    }

    private fun stringLiteral() {
        val sb = StringBuilder()
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
                column = 0
            }
            if (peek() == '\\') {
                advance()
                when (val esc = advance()) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    'r' -> sb.append('\r')
                    '0' -> sb.append('\u0000')
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    else -> sb.append(esc)
                }
            } else {
                sb.append(advance())
            }
        }

        if (isAtEnd()) {
            // Unterminated string
            addToken(TokenType.STRING_LITERAL, sb.toString())
            return
        }

        advance() // The closing '"'
        addToken(TokenType.STRING_LITERAL, sb.toString())
    }

    private fun charLiteral() {
        var charVal = '\u0000'
        if (peek() == '\\') {
            advance()
            charVal = when (val esc = advance()) {
                'n' -> '\n'
                't' -> '\t'
                'r' -> '\r'
                '0' -> '\u0000'
                '\'' -> '\''
                '\\' -> '\\'
                else -> esc
            }
        } else if (peek() != '\'') {
            charVal = advance()
        }

        if (peek() == '\'') {
            advance()
        }
        addToken(TokenType.CHAR_LITERAL, charVal.code)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        column++
        return true
    }

    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]
    private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]

    private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isDigit(c: Char): Boolean = c in '0'..'9'
    private fun isHexDigit(c: Char): Boolean = isDigit(c) || c in 'a'..'f' || c in 'A'..'F'
    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    private fun isAtEnd(): Boolean = current >= source.length

    private fun advance(): Char {
        val c = source[current++]
        column++
        return c
    }

    private fun addToken(type: TokenType, value: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, value, line, tokenStartColumn))
    }
}
