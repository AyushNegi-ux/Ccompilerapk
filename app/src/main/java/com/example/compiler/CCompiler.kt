package com.example.compiler

object CCompiler {
    fun compileAndRun(
        sourceCode: String,
        stdinInput: String = "",
        isDebugMode: Boolean = false
    ): CompilerResult {
        val startTime = System.currentTimeMillis()

        if (sourceCode.isBlank()) {
            return CompilerResult(
                isSuccess = false,
                exitCode = 1,
                output = "",
                errorOutput = "Compiler error: No source code provided to compile.\n",
                diagnostics = listOf(CompilerDiagnostic(1, 1, "Empty source code", true)),
                metrics = ExecutionMetrics(0, 0, 0)
            )
        }

        // 1. Lexical Analysis
        val tokens = try {
            val lexer = Lexer(sourceCode)
            lexer.tokenize()
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            return CompilerResult(
                isSuccess = false,
                exitCode = 1,
                output = "",
                errorOutput = "Lexical error: ${e.message ?: "Failed to tokenize source code"}\n",
                diagnostics = listOf(CompilerDiagnostic(1, 1, e.message ?: "Token error", true)),
                metrics = ExecutionMetrics(totalTime, 0, 0)
            )
        }

        // 2. Syntax Analysis (Parsing)
        val program = try {
            val parser = Parser(tokens)
            parser.parse()
        } catch (pe: ParseException) {
            val totalTime = System.currentTimeMillis() - startTime
            val formattedError = "main.c:${pe.line}:${pe.column}: error: ${pe.message}\n"
            return CompilerResult(
                isSuccess = false,
                exitCode = 1,
                output = "",
                errorOutput = formattedError,
                diagnostics = listOf(CompilerDiagnostic(pe.line, pe.column, pe.message, true)),
                metrics = ExecutionMetrics(totalTime, 0, 0)
            )
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            val msg = e.message ?: "Syntax error"
            return CompilerResult(
                isSuccess = false,
                exitCode = 1,
                output = "",
                errorOutput = "Compilation error: $msg\n",
                diagnostics = listOf(CompilerDiagnostic(1, 1, msg, true)),
                metrics = ExecutionMetrics(totalTime, 0, 0)
            )
        }

        // 3. Execution (Offline Virtual Machine)
        return try {
            val interpreter = CInterpreter(program, stdinInput, isDebugMode)
            interpreter.execute()
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            val msg = e.message ?: "Runtime error"
            CompilerResult(
                isSuccess = false,
                exitCode = 1,
                output = "",
                errorOutput = "Runtime crash: $msg\n",
                diagnostics = listOf(CompilerDiagnostic(1, 1, msg, true)),
                metrics = ExecutionMetrics(totalTime, 0, 0)
            )
        }
    }
}
