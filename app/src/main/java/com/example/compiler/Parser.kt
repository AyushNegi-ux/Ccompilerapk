package com.example.compiler

class ParseException(val line: Int, val column: Int, override val message: String) : Exception("Error at line $line, col $column: $message")

class Parser(private val tokens: List<Token>) {
    private var current = 0
    private val includes = mutableListOf<String>()
    private val defines = mutableMapOf<String, String>()

    fun parse(): Program {
        val structs = mutableListOf<StructDef>()
        val globals = mutableListOf<VarDeclStmt>()
        val functions = mutableListOf<FunctionDef>()

        while (!isAtEnd()) {
            if (check(TokenType.PREPROCESSOR)) {
                handlePreprocessor()
                continue
            }

            if (check(TokenType.SEMICOLON)) {
                advance()
                continue
            }

            if (check(TokenType.KEYWORD_STRUCT) && peekNext().type == TokenType.IDENTIFIER && peekThird().type == TokenType.LBRACE) {
                structs.add(parseStructDef())
                continue
            }

            // Top-level declaration: could be a function or global variable
            val startToken = peek()
            val parsedType = parseType() ?: throw ParseException(startToken.line, startToken.column, "Expected type specifier at top level")

            val nameToken = consume(TokenType.IDENTIFIER, "Expected identifier after type")
            val name = nameToken.lexeme

            if (check(TokenType.LPAREN)) {
                // Function definition
                val functionDef = parseFunctionDef(parsedType, name)
                functions.add(functionDef)
            } else {
                // Global variable declaration
                val varDecls = parseVarDeclList(parsedType, name)
                globals.addAll(varDecls)
                consume(TokenType.SEMICOLON, "Expected ';' after global variable declaration")
            }
        }

        return Program(structs, globals, functions, includes, defines)
    }

    private fun handlePreprocessor() {
        val token = advance()
        val text = token.lexeme.trim()
        if (text.startsWith("#include")) {
            val inc = text.substring(8).trim().trim('<', '>', '"')
            includes.add(inc)
        } else if (text.startsWith("#define")) {
            val parts = text.substring(7).trim().split("\\s+".toRegex(), limit = 2)
            if (parts.isNotEmpty()) {
                val key = parts[0]
                val value = if (parts.size > 1) parts[1] else "1"
                defines[key] = value
            }
        }
    }

    private fun parseStructDef(): StructDef {
        consume(TokenType.KEYWORD_STRUCT, "Expected 'struct'")
        val name = consume(TokenType.IDENTIFIER, "Expected struct name").lexeme
        consume(TokenType.LBRACE, "Expected '{' in struct definition")
        val members = mutableListOf<Pair<CType, String>>()
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            val memberType = parseType() ?: throw ParseException(peek().line, peek().column, "Expected type in struct member")
            val memberName = consume(TokenType.IDENTIFIER, "Expected member name").lexeme
            members.add(Pair(memberType, memberName))
            consume(TokenType.SEMICOLON, "Expected ';' after struct member")
        }
        consume(TokenType.RBRACE, "Expected '}' after struct members")
        consume(TokenType.SEMICOLON, "Expected ';' after struct definition")
        return StructDef(name, members)
    }

    private fun parseFunctionDef(returnType: CType, name: String): FunctionDef {
        consume(TokenType.LPAREN, "Expected '(' after function name")
        val params = mutableListOf<Parameter>()
        if (!check(TokenType.RPAREN)) {
            do {
                if (check(TokenType.KEYWORD_VOID) && (checkNext(TokenType.RPAREN))) {
                    advance() // (void) parameter list
                    break
                }
                val paramType = parseType() ?: throw ParseException(peek().line, peek().column, "Expected parameter type")
                var paramName = ""
                if (check(TokenType.IDENTIFIER)) {
                    paramName = advance().lexeme
                }
                // Handle array parameters e.g. int arr[]
                var finalType = paramType
                if (check(TokenType.LBRACKET)) {
                    advance()
                    if (check(TokenType.INT_LITERAL)) advance()
                    consume(TokenType.RBRACKET, "Expected ']' in array parameter")
                    finalType = paramType.copy(pointerLevel = paramType.pointerLevel + 1)
                }
                params.add(Parameter(finalType, paramName))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RPAREN, "Expected ')' after parameters")

        // In C, could be prototype with semicolon
        if (check(TokenType.SEMICOLON)) {
            advance()
            return FunctionDef(returnType, name, params, BlockStmt(emptyList()))
        }

        val body = parseBlockStmt()
        val func = FunctionDef(returnType, name, params, body)
        func.line = returnType.pointerLevel // dummy or start line
        return func
    }

    private fun parseBlockStmt(): BlockStmt {
        val openBrace = consume(TokenType.LBRACE, "Expected '{'")
        val statements = mutableListOf<StmtNode>()
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            val stmt = parseStatement()
            if (stmt != null) {
                statements.add(stmt)
            }
        }
        consume(TokenType.RBRACE, "Expected '}' at end of block")
        val block = BlockStmt(statements)
        block.line = openBrace.line
        return block
    }

    private fun parseStatement(): StmtNode? {
        if (check(TokenType.SEMICOLON)) {
            advance()
            return null
        }

        if (check(TokenType.LBRACE)) {
            return parseBlockStmt()
        }

        if (match(TokenType.KEYWORD_IF)) {
            return parseIfStmt()
        }

        if (match(TokenType.KEYWORD_WHILE)) {
            return parseWhileStmt()
        }

        if (match(TokenType.KEYWORD_DO)) {
            return parseDoWhileStmt()
        }

        if (match(TokenType.KEYWORD_FOR)) {
            return parseForStmt()
        }

        if (match(TokenType.KEYWORD_SWITCH)) {
            return parseSwitchStmt()
        }

        if (match(TokenType.KEYWORD_CASE)) {
            val valExpr = parseExpression()
            consume(TokenType.COLON, "Expected ':' after case value")
            val constVal = (valExpr as? IntLiteralExpr)?.value ?: 0
            val stmt = CaseStmt(constVal, false)
            stmt.line = peek().line
            return stmt
        }

        if (match(TokenType.KEYWORD_DEFAULT)) {
            consume(TokenType.COLON, "Expected ':' after default")
            val stmt = CaseStmt(null, true)
            stmt.line = peek().line
            return stmt
        }

        if (match(TokenType.KEYWORD_RETURN)) {
            val tokenLine = previous().line
            val expr = if (!check(TokenType.SEMICOLON)) parseExpression() else null
            consume(TokenType.SEMICOLON, "Expected ';' after return")
            val ret = ReturnStmt(expr)
            ret.line = tokenLine
            return ret
        }

        if (match(TokenType.KEYWORD_BREAK)) {
            val tokenLine = previous().line
            consume(TokenType.SEMICOLON, "Expected ';' after break")
            val stmt = BreakStmt()
            stmt.line = tokenLine
            return stmt
        }

        if (match(TokenType.KEYWORD_CONTINUE)) {
            val tokenLine = previous().line
            consume(TokenType.SEMICOLON, "Expected ';' after continue")
            val stmt = ContinueStmt()
            stmt.line = tokenLine
            return stmt
        }

        // Variable declaration? Check if current token starts a type
        val type = parseType()
        if (type != null) {
            val firstId = consume(TokenType.IDENTIFIER, "Expected identifier in variable declaration").lexeme
            val decls = parseVarDeclList(type, firstId)
            consume(TokenType.SEMICOLON, "Expected ';' after declaration")
            return if (decls.size == 1) decls[0] else BlockStmt(decls)
        }

        // Expression statement
        val expr = parseExpression()
        consume(TokenType.SEMICOLON, "Expected ';' after expression")
        val stmt = ExprStmt(expr)
        stmt.line = expr.line
        return stmt
    }

    private fun parseVarDeclList(type: CType, firstIdent: String): List<VarDeclStmt> {
        val list = mutableListOf<VarDeclStmt>()
        var curName = firstIdent
        var curType = type

        while (true) {
            var dims = mutableListOf<Int>()
            while (match(TokenType.LBRACKET)) {
                if (check(TokenType.INT_LITERAL)) {
                    val sizeToken = advance()
                    dims.add(sizeToken.value as Int)
                } else if (check(TokenType.IDENTIFIER)) {
                    // Constant define or variable
                    val idToken = advance()
                    val resolved = defines[idToken.lexeme]?.toIntOrNull() ?: 10
                    dims.add(resolved)
                } else {
                    dims.add(-1) // dynamically sized or inferred
                }
                consume(TokenType.RBRACKET, "Expected ']' in array dimension")
            }

            val finalType = if (dims.isNotEmpty()) {
                curType.copy(arrayDimensions = dims)
            } else {
                curType
            }

            var initializer: ExprNode? = null
            var arrayInitializer: List<ExprNode>? = null

            if (match(TokenType.ASSIGN)) {
                if (match(TokenType.LBRACE)) {
                    // Array initializer list { 1, 2, 3 }
                    val elements = mutableListOf<ExprNode>()
                    if (!check(TokenType.RBRACE)) {
                        do {
                            elements.add(parseExpression())
                        } while (match(TokenType.COMMA) && !check(TokenType.RBRACE))
                    }
                    consume(TokenType.RBRACE, "Expected '}' in array initializer")
                    arrayInitializer = elements
                } else {
                    initializer = parseExpression()
                }
            }

            val decl = VarDeclStmt(finalType, curName, initializer, arrayInitializer)
            decl.line = peek().line
            list.add(decl)

            if (!match(TokenType.COMMA)) break

            // Next variable might have pointer prefix: int a, *b;
            var ptrLevel = type.pointerLevel
            while (match(TokenType.STAR)) {
                ptrLevel++
            }
            curType = type.copy(pointerLevel = ptrLevel)
            curName = consume(TokenType.IDENTIFIER, "Expected identifier after ','").lexeme
        }

        return list
    }

    private fun parseIfStmt(): IfStmt {
        val tokenLine = previous().line
        consume(TokenType.LPAREN, "Expected '(' after 'if'")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after if condition")
        val thenBranch = parseStatement() ?: BlockStmt(emptyList())
        val elseBranch = if (match(TokenType.KEYWORD_ELSE)) parseStatement() else null
        val stmt = IfStmt(condition, thenBranch, elseBranch)
        stmt.line = tokenLine
        return stmt
    }

    private fun parseWhileStmt(): WhileStmt {
        val tokenLine = previous().line
        consume(TokenType.LPAREN, "Expected '(' after 'while'")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after while condition")
        val body = parseStatement() ?: BlockStmt(emptyList())
        val stmt = WhileStmt(condition, body)
        stmt.line = tokenLine
        return stmt
    }

    private fun parseDoWhileStmt(): DoWhileStmt {
        val tokenLine = previous().line
        val body = parseStatement() ?: BlockStmt(emptyList())
        consume(TokenType.KEYWORD_WHILE, "Expected 'while' after do block")
        consume(TokenType.LPAREN, "Expected '(' after 'while'")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after while condition")
        consume(TokenType.SEMICOLON, "Expected ';' after do-while")
        val stmt = DoWhileStmt(body, condition)
        stmt.line = tokenLine
        return stmt
    }

    private fun parseForStmt(): ForStmt {
        val tokenLine = previous().line
        consume(TokenType.LPAREN, "Expected '(' after 'for'")

        // Init
        val init = if (match(TokenType.SEMICOLON)) {
            null
        } else {
            val type = parseType()
            if (type != null) {
                val name = consume(TokenType.IDENTIFIER, "Expected identifier in for init").lexeme
                val decls = parseVarDeclList(type, name)
                consume(TokenType.SEMICOLON, "Expected ';' after for loop init")
                if (decls.size == 1) decls[0] else BlockStmt(decls)
            } else {
                val expr = parseExpression()
                consume(TokenType.SEMICOLON, "Expected ';' after for loop init")
                ExprStmt(expr)
            }
        }

        // Condition
        val condition = if (!check(TokenType.SEMICOLON)) parseExpression() else null
        consume(TokenType.SEMICOLON, "Expected ';' after for loop condition")

        // Step
        val step = if (!check(TokenType.RPAREN)) parseExpression() else null
        consume(TokenType.RPAREN, "Expected ')' after for loop clauses")

        val body = parseStatement() ?: BlockStmt(emptyList())
        val stmt = ForStmt(init, condition, step, body)
        stmt.line = tokenLine
        return stmt
    }

    private fun parseSwitchStmt(): SwitchStmt {
        val tokenLine = previous().line
        consume(TokenType.LPAREN, "Expected '(' after 'switch'")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after switch condition")
        val body = parseStatement() ?: BlockStmt(emptyList())
        val stmt = SwitchStmt(condition, body)
        stmt.line = tokenLine
        return stmt
    }

    // Expressions
    fun parseExpression(): ExprNode = parseAssignment()

    private fun parseAssignment(): ExprNode {
        val expr = parseTernary()

        if (match(TokenType.ASSIGN, TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN, TokenType.STAR_ASSIGN, TokenType.SLASH_ASSIGN, TokenType.PERCENT_ASSIGN)) {
            val op = previous().lexeme
            val value = parseAssignment()
            val node = AssignExpr(expr, op, value)
            node.line = expr.line
            return node
        }

        return expr
    }

    private fun parseTernary(): ExprNode {
        val expr = parseLogicalOr()
        if (match(TokenType.QUESTION)) {
            val thenBranch = parseExpression()
            consume(TokenType.COLON, "Expected ':' in ternary operator")
            val elseBranch = parseTernary()
            val node = TernaryExpr(expr, thenBranch, elseBranch)
            node.line = expr.line
            return node
        }
        return expr
    }

    private fun parseLogicalOr(): ExprNode {
        var expr = parseLogicalAnd()
        while (match(TokenType.LOGICAL_OR)) {
            val op = previous().lexeme
            val right = parseLogicalAnd()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseLogicalAnd(): ExprNode {
        var expr = parseBitwiseOr()
        while (match(TokenType.LOGICAL_AND)) {
            val op = previous().lexeme
            val right = parseBitwiseOr()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseBitwiseOr(): ExprNode {
        var expr = parseBitwiseXor()
        while (match(TokenType.PIPE)) {
            val op = previous().lexeme
            val right = parseBitwiseXor()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseBitwiseXor(): ExprNode {
        var expr = parseBitwiseAnd()
        while (match(TokenType.CARET)) {
            val op = previous().lexeme
            val right = parseBitwiseAnd()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseBitwiseAnd(): ExprNode {
        var expr = parseEquality()
        while (match(TokenType.AMPERSAND)) {
            val op = previous().lexeme
            val right = parseEquality()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseEquality(): ExprNode {
        var expr = parseRelational()
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            val op = previous().lexeme
            val right = parseRelational()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseRelational(): ExprNode {
        var expr = parseShift()
        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            val op = previous().lexeme
            val right = parseShift()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseShift(): ExprNode {
        var expr = parseAdditive()
        while (match(TokenType.SHL, TokenType.SHR)) {
            val op = previous().lexeme
            val right = parseAdditive()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseAdditive(): ExprNode {
        var expr = parseMultiplicative()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous().lexeme
            val right = parseMultiplicative()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseMultiplicative(): ExprNode {
        var expr = parseUnary()
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            val op = previous().lexeme
            val right = parseUnary()
            val binary = BinaryExpr(expr, op, right)
            binary.line = expr.line
            expr = binary
        }
        return expr
    }

    private fun parseUnary(): ExprNode {
        if (match(TokenType.BANG, TokenType.TILDE, TokenType.MINUS, TokenType.PLUS, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, TokenType.STAR, TokenType.AMPERSAND)) {
            val op = previous().lexeme
            val tokenLine = previous().line
            val operand = parseUnary()
            val node = UnaryExpr(op, operand, isPrefix = true)
            node.line = tokenLine
            return node
        }

        if (match(TokenType.KEYWORD_SIZEOF)) {
            val tokenLine = previous().line
            consume(TokenType.LPAREN, "Expected '(' after sizeof")
            val type = parseType()
            val node = if (type != null) {
                consume(TokenType.RPAREN, "Expected ')' after sizeof type")
                SizeofExpr(type, null)
            } else {
                val expr = parseExpression()
                consume(TokenType.RPAREN, "Expected ')' after sizeof expr")
                SizeofExpr(null, expr)
            }
            node.line = tokenLine
            return node
        }

        // Explicit cast: e.g. (int)x, (float)y
        if (check(TokenType.LPAREN) && checkTypeAhead()) {
            advance() // '('
            val castType = parseType() ?: throw ParseException(peek().line, peek().column, "Expected type in cast")
            consume(TokenType.RPAREN, "Expected ')' after cast type")
            val operand = parseUnary()
            val node = CastExpr(castType, operand)
            node.line = operand.line
            return node
        }

        return parsePostfix()
    }

    private fun parsePostfix(): ExprNode {
        var expr = parsePrimary()

        while (true) {
            if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
                val op = previous().lexeme
                val node = UnaryExpr(op, expr, isPrefix = false)
                node.line = expr.line
                expr = node
            } else if (match(TokenType.LBRACKET)) {
                val index = parseExpression()
                consume(TokenType.RBRACKET, "Expected ']' after array index")
                val node = ArrayAccessExpr(expr, index)
                node.line = expr.line
                expr = node
            } else if (match(TokenType.DOT, TokenType.ARROW)) {
                val isArrow = previous().type == TokenType.ARROW
                val memberName = consume(TokenType.IDENTIFIER, "Expected member name after '.' or '->'").lexeme
                val node = MemberAccessExpr(expr, memberName, isArrow)
                node.line = expr.line
                expr = node
            } else if (match(TokenType.LPAREN)) {
                val args = mutableListOf<ExprNode>()
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(parseExpression())
                    } while (match(TokenType.COMMA))
                }
                consume(TokenType.RPAREN, "Expected ')' after arguments")
                val calleeName = (expr as? VariableExpr)?.name ?: "unknown"
                val node = CallExpr(calleeName, args)
                node.line = expr.line
                expr = node
            } else {
                break
            }
        }

        return expr
    }

    private fun parsePrimary(): ExprNode {
        val token = peek()
        return when {
            match(TokenType.INT_LITERAL) -> {
                val node = IntLiteralExpr(token.value as Int)
                node.line = token.line
                node
            }
            match(TokenType.FLOAT_LITERAL) -> {
                val node = FloatLiteralExpr(token.value as Double)
                node.line = token.line
                node
            }
            match(TokenType.CHAR_LITERAL) -> {
                val node = CharLiteralExpr(token.value as Int)
                node.line = token.line
                node
            }
            match(TokenType.STRING_LITERAL) -> {
                val node = StringLiteralExpr(token.value as String)
                node.line = token.line
                node
            }
            match(TokenType.IDENTIFIER) -> {
                // Check if it's a defined macro
                val macroVal = defines[token.lexeme]
                if (macroVal != null && macroVal.toIntOrNull() != null) {
                    val node = IntLiteralExpr(macroVal.toInt())
                    node.line = token.line
                    node
                } else {
                    val node = VariableExpr(token.lexeme)
                    node.line = token.line
                    node
                }
            }
            match(TokenType.LPAREN) -> {
                val expr = parseExpression()
                consume(TokenType.RPAREN, "Expected ')' after expression")
                expr
            }
            else -> {
                throw ParseException(token.line, token.column, "Unexpected token '${token.lexeme}' in expression")
            }
        }
    }

    private fun checkTypeAhead(): Boolean {
        var idx = current + 1
        if (idx >= tokens.size) return false
        val t = tokens[idx].type
        return t == TokenType.KEYWORD_INT || t == TokenType.KEYWORD_FLOAT ||
               t == TokenType.KEYWORD_CHAR || t == TokenType.KEYWORD_DOUBLE ||
               t == TokenType.KEYWORD_VOID || t == TokenType.KEYWORD_STRUCT
    }

    private fun parseType(): CType? {
        var isConst = false
        if (match(TokenType.KEYWORD_CONST)) isConst = true

        val token = peek()
        val baseType = when (token.type) {
            TokenType.KEYWORD_INT -> { advance(); "int" }
            TokenType.KEYWORD_FLOAT -> { advance(); "float" }
            TokenType.KEYWORD_CHAR -> { advance(); "char" }
            TokenType.KEYWORD_DOUBLE -> { advance(); "double" }
            TokenType.KEYWORD_VOID -> { advance(); "void" }
            TokenType.KEYWORD_STRUCT -> {
                advance()
                val structName = consume(TokenType.IDENTIFIER, "Expected struct name").lexeme
                var ptrLevel = 0
                while (match(TokenType.STAR)) ptrLevel++
                return CType("struct", ptrLevel, emptyList(), structName)
            }
            else -> return null
        }

        var ptrLevel = 0
        while (match(TokenType.STAR)) {
            ptrLevel++
        }

        return CType(baseType, ptrLevel)
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun checkNext(type: TokenType): Boolean {
        if (current + 1 >= tokens.size) return false
        return tokens[current + 1].type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF
    private fun peek(): Token = tokens[current]
    private fun peekNext(): Token = if (current + 1 < tokens.size) tokens[current + 1] else tokens.last()
    private fun peekThird(): Token = if (current + 2 < tokens.size) tokens[current + 2] else tokens.last()
    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        val t = peek()
        throw ParseException(t.line, t.column, message)
    }
}
