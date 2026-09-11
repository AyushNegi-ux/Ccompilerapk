package com.example.compiler

data class CompilerDiagnostic(
    val line: Int,
    val column: Int,
    val message: String,
    val isError: Boolean = true
)

data class DebugVariable(
    val name: String,
    val type: String,
    val value: String,
    val memoryAddress: String
)

data class DebugSnapshot(
    val stepIndex: Int,
    val line: Int,
    val description: String,
    val callStack: List<String>,
    val variables: List<DebugVariable>
)

data class ExecutionMetrics(
    val executionTimeMs: Long,
    val memoryAllocatedBytes: Int,
    val instructionsExecuted: Int
)

data class CompilerResult(
    val isSuccess: Boolean,
    val exitCode: Int,
    val output: String,
    val errorOutput: String,
    val diagnostics: List<CompilerDiagnostic>,
    val metrics: ExecutionMetrics,
    val debugSnapshots: List<DebugSnapshot> = emptyList()
)
