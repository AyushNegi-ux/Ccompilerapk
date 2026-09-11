package com.example.compiler

import java.util.Scanner
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class RuntimeError(val line: Int, override val message: String) : Exception("Runtime Error at line $line: $message")
class ReturnException(val value: RuntimeValue) : Exception()
class BreakException : Exception()
class ContinueException : Exception()

class CInterpreter(
    private val program: Program,
    private val stdin: String = "",
    private val isDebugMode: Boolean = false
) {
    private val stdout = StringBuilder()
    private val stderr = StringBuilder()
    private val diagnostics = mutableListOf<CompilerDiagnostic>()
    private val debugSnapshots = mutableListOf<DebugSnapshot>()

    // Memory simulation
    private var nextAddress = 0x1000
    private val memory = mutableMapOf<Int, RuntimeValue>() // address -> value
    private val variableAddresses = mutableMapOf<String, Int>() // varName -> address

    // Call stack
    private val callStack = ArrayDeque<MutableMap<String, RuntimeValue>>()
    private val frameAddresses = ArrayDeque<MutableMap<String, Int>>()
    private val stackFrameNames = ArrayDeque<String>()
    private val functions = mutableMapOf<String, FunctionDef>()

    // Scanner for scanf
    private val inputScanner = Scanner(stdin)
    private var instructionCount = 0
    private val MAX_INSTRUCTIONS = 250_000

    init {
        for (f in program.functions) {
            functions[f.name] = f
        }
    }

    fun execute(): CompilerResult {
        val startTime = System.currentTimeMillis()
        var exitCode = 0
        var isSuccess = true

        try {
            // Global scope
            val globalScope = mutableMapOf<String, RuntimeValue>()
            callStack.addLast(globalScope)
            frameAddresses.addLast(mutableMapOf())
            stackFrameNames.addLast("global")

            // Initialize globals
            for (globalDecl in program.globals) {
                executeVarDecl(globalDecl)
            }

            // Find main
            val mainFunc = functions["main"]
            if (mainFunc == null) {
                diagnostics.add(CompilerDiagnostic(1, 1, "Undefined reference to 'main': In C, 'int main()' is required.", true))
                stderr.append("linker error: undefined reference to 'main'\n")
                return CompilerResult(
                    isSuccess = false,
                    exitCode = 1,
                    output = stdout.toString(),
                    errorOutput = stderr.toString(),
                    diagnostics = diagnostics,
                    metrics = ExecutionMetrics(0, memory.size * 4, 0),
                    debugSnapshots = emptyList()
                )
            }

            // Execute main
            val mainResult = executeFunction(mainFunc, emptyList())
            exitCode = mainResult.asInt()

        } catch (re: RuntimeError) {
            isSuccess = false
            exitCode = 1
            diagnostics.add(CompilerDiagnostic(re.line, 1, re.message, true))
            stderr.append("Runtime Error: ${re.message}\n")
        } catch (e: Exception) {
            isSuccess = false
            exitCode = 1
            stderr.append("Execution error: ${e.message ?: e.toString()}\n")
            diagnostics.add(CompilerDiagnostic(1, 1, e.message ?: "Execution error", true))
        }

        val totalTime = System.currentTimeMillis() - startTime
        return CompilerResult(
            isSuccess = isSuccess,
            exitCode = exitCode,
            output = stdout.toString(),
            errorOutput = stderr.toString(),
            diagnostics = diagnostics,
            metrics = ExecutionMetrics(totalTime, memory.size * 4, instructionCount),
            debugSnapshots = debugSnapshots
        )
    }

    private fun executeFunction(func: FunctionDef, argValues: List<RuntimeValue>): RuntimeValue {
        val frame = mutableMapOf<String, RuntimeValue>()
        stackFrameNames.addLast(func.name)
        callStack.addLast(frame)
        frameAddresses.addLast(mutableMapOf())

        for (i in func.parameters.indices) {
            val param = func.parameters[i]
            val value = if (i < argValues.size) argValues[i] else CInt(0)
            setVariable(param.name, value, isDecl = true)
        }

        if (isDebugMode) {
            recordDebugSnapshot(func.body.line, "Call ${func.name}(${argValues.joinToString { it.toDisplayString() }})")
        }

        var result: RuntimeValue = CInt(0)
        try {
            executeBlock(func.body)
        } catch (ret: ReturnException) {
            result = ret.value
        } finally {
            callStack.removeLast()
            frameAddresses.removeLast()
            stackFrameNames.removeLast()
        }

        return result
    }

    private fun executeStatement(stmt: StmtNode) {
        checkInstructions(stmt.line)

        when (stmt) {
            is ExprStmt -> {
                evaluateExpr(stmt.expr)
                if (isDebugMode) {
                    recordDebugSnapshot(stmt.line, "Statement executed")
                }
            }
            is VarDeclStmt -> {
                executeVarDecl(stmt)
                if (isDebugMode) {
                    recordDebugSnapshot(stmt.line, "Declared variable ${stmt.name}")
                }
            }
            is BlockStmt -> executeBlock(stmt)
            is IfStmt -> executeIf(stmt)
            is WhileStmt -> executeWhile(stmt)
            is DoWhileStmt -> executeDoWhile(stmt)
            is ForStmt -> executeFor(stmt)
            is SwitchStmt -> executeSwitch(stmt)
            is ReturnStmt -> {
                val value = stmt.expr?.let { evaluateExpr(it) } ?: CVoid
                throw ReturnException(value)
            }
            is BreakStmt -> throw BreakException()
            is ContinueStmt -> throw ContinueException()
            is CaseStmt -> {
                // Handled in switch execution
            }
        }
    }

    private fun executeBlock(block: BlockStmt) {
        for (s in block.statements) {
            executeStatement(s)
        }
    }

    private fun executeVarDecl(stmt: VarDeclStmt) {
        val initialValue: RuntimeValue = when {
            stmt.arrayInitializer != null -> {
                val elements = stmt.arrayInitializer.map { evaluateExpr(it) }.toMutableList()
                CArray(elements, stmt.type.baseType)
            }
            stmt.type.arrayDimensions.isNotEmpty() -> {
                val size = stmt.type.arrayDimensions.firstOrNull()?.takeIf { it > 0 } ?: 10
                if (stmt.type.baseType == "char" && stmt.initializer is StringLiteralExpr) {
                    CString(stmt.initializer.value)
                } else {
                    val elements = MutableList<RuntimeValue>(size) {
                        if (stmt.type.baseType == "float" || stmt.type.baseType == "double") CFloat(0.0) else CInt(0)
                    }
                    CArray(elements, stmt.type.baseType)
                }
            }
            stmt.initializer != null -> {
                evaluateExpr(stmt.initializer)
            }
            else -> {
                when (stmt.type.baseType) {
                    "float", "double" -> CFloat(0.0)
                    "char" -> CChar(0)
                    "struct" -> {
                        val members = mutableMapOf<String, RuntimeValue>()
                        val sDef = program.structs.find { it.name == stmt.type.structName }
                        sDef?.members?.forEach { (mType, mName) ->
                            members[mName] = if (mType.baseType == "float") CFloat(0.0) else CInt(0)
                        }
                        CStruct(stmt.type.structName ?: "unknown", members)
                    }
                    else -> CInt(0)
                }
            }
        }

        // Allocate address
        val addr = nextAddress
        nextAddress += 4
        memory[addr] = initialValue
        variableAddresses[stmt.name] = addr

        setVariable(stmt.name, initialValue, isDecl = true)
    }

    private fun executeIf(stmt: IfStmt) {
        val cond = evaluateExpr(stmt.condition)
        if (cond.asBoolean()) {
            executeStatement(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            executeStatement(stmt.elseBranch)
        }
    }

    private fun executeWhile(stmt: WhileStmt) {
        while (evaluateExpr(stmt.condition).asBoolean()) {
            try {
                executeStatement(stmt.body)
            } catch (b: BreakException) {
                break
            } catch (c: ContinueException) {
                continue
            }
        }
    }

    private fun executeDoWhile(stmt: DoWhileStmt) {
        do {
            try {
                executeStatement(stmt.body)
            } catch (b: BreakException) {
                break
            } catch (c: ContinueException) {
                continue
            }
        } while (evaluateExpr(stmt.condition).asBoolean())
    }

    private fun executeFor(stmt: ForStmt) {
        if (stmt.init != null) {
            executeStatement(stmt.init)
        }
        while (stmt.condition == null || evaluateExpr(stmt.condition).asBoolean()) {
            try {
                executeStatement(stmt.body)
            } catch (b: BreakException) {
                break
            } catch (c: ContinueException) {
                // fall through to step
            }
            if (stmt.step != null) {
                evaluateExpr(stmt.step)
            }
        }
    }

    private fun executeSwitch(stmt: SwitchStmt) {
        val switchVal = evaluateExpr(stmt.condition).asInt()
        if (stmt.body is BlockStmt) {
            var matched = false
            var defaultIndex = -1
            val stmts = stmt.body.statements

            for (i in stmts.indices) {
                val s = stmts[i]
                if (s is CaseStmt) {
                    if (s.isDefault) {
                        defaultIndex = i
                    } else if (s.value == switchVal) {
                        matched = true
                        for (j in i + 1 until stmts.size) {
                            try {
                                if (stmts[j] !is CaseStmt) executeStatement(stmts[j])
                            } catch (b: BreakException) {
                                return
                            }
                        }
                        return
                    }
                }
            }

            if (!matched && defaultIndex >= 0) {
                for (j in defaultIndex + 1 until stmts.size) {
                    try {
                        if (stmts[j] !is CaseStmt) executeStatement(stmts[j])
                    } catch (b: BreakException) {
                        return
                    }
                }
            }
        }
    }

    // Expressions
    private fun evaluateExpr(expr: ExprNode): RuntimeValue {
        checkInstructions(expr.line)

        return when (expr) {
            is IntLiteralExpr -> CInt(expr.value)
            is FloatLiteralExpr -> CFloat(expr.value)
            is CharLiteralExpr -> CChar(expr.value)
            is StringLiteralExpr -> CString(expr.value)
            is VariableExpr -> getVariable(expr.name, expr.line)

            is BinaryExpr -> evaluateBinary(expr)
            is UnaryExpr -> evaluateUnary(expr)
            is AssignExpr -> evaluateAssign(expr)
            is CallExpr -> evaluateCall(expr)
            is ArrayAccessExpr -> evaluateArrayAccess(expr)
            is MemberAccessExpr -> evaluateMemberAccess(expr)
            is TernaryExpr -> {
                val cond = evaluateExpr(expr.condition)
                if (cond.asBoolean()) evaluateExpr(expr.thenBranch) else evaluateExpr(expr.elseBranch)
            }
            is SizeofExpr -> {
                val size = if (expr.targetType != null) {
                    when (expr.targetType.baseType) {
                        "char" -> 1
                        "int", "float" -> 4
                        "double" -> 8
                        else -> 4
                    }
                } else if (expr.targetExpr != null) {
                    val v = evaluateExpr(expr.targetExpr)
                    when (v) {
                        is CChar -> 1
                        is CFloat -> 8
                        is CArray -> v.elements.size * 4
                        is CString -> v.text.length + 1
                        else -> 4
                    }
                } else 4
                CInt(size)
            }
            is CastExpr -> {
                val value = evaluateExpr(expr.expr)
                when (expr.targetType.baseType) {
                    "int" -> CInt(value.asInt())
                    "float", "double" -> CFloat(value.asFloat())
                    "char" -> CChar(value.asInt())
                    else -> value
                }
            }
        }
    }

    private fun evaluateBinary(expr: BinaryExpr): RuntimeValue {
        val left = evaluateExpr(expr.left)

        // Short-circuit logical ops
        if (expr.op == "&&") {
            return if (!left.asBoolean()) CInt(0) else CInt(if (evaluateExpr(expr.right).asBoolean()) 1 else 0)
        }
        if (expr.op == "||") {
            return if (left.asBoolean()) CInt(1) else CInt(if (evaluateExpr(expr.right).asBoolean()) 1 else 0)
        }

        val right = evaluateExpr(expr.right)

        // Pointer arithmetic
        if (left is CPointer && right is CInt) {
            return when (expr.op) {
                "+" -> CPointer(left.address, left.offset + right.value * 4, left.targetType)
                "-" -> CPointer(left.address, left.offset - right.value * 4, left.targetType)
                else -> CInt(0)
            }
        }

        // Float arithmetic if either operand is float
        if (left is CFloat || right is CFloat) {
            val l = left.asFloat()
            val r = right.asFloat()
            return when (expr.op) {
                "+" -> CFloat(l + r)
                "-" -> CFloat(l - r)
                "*" -> CFloat(l * r)
                "/" -> if (r == 0.0) throw RuntimeError(expr.line, "Floating point division by zero") else CFloat(l / r)
                "==" -> CInt(if (l == r) 1 else 0)
                "!=" -> CInt(if (l != r) 1 else 0)
                "<" -> CInt(if (l < r) 1 else 0)
                "<=" -> CInt(if (l <= r) 1 else 0)
                ">" -> CInt(if (l > r) 1 else 0)
                ">=" -> CInt(if (l >= r) 1 else 0)
                else -> CFloat(0.0)
            }
        }

        // Integer arithmetic
        val l = left.asInt()
        val r = right.asInt()
        return when (expr.op) {
            "+" -> CInt(l + r)
            "-" -> CInt(l - r)
            "*" -> CInt(l * r)
            "/" -> if (r == 0) throw RuntimeError(expr.line, "Integer division by zero") else CInt(l / r)
            "%" -> if (r == 0) throw RuntimeError(expr.line, "Modulo by zero") else CInt(l % r)
            "==" -> CInt(if (l == r) 1 else 0)
            "!=" -> CInt(if (l != r) 1 else 0)
            "<" -> CInt(if (l < r) 1 else 0)
            "<=" -> CInt(if (l <= r) 1 else 0)
            ">" -> CInt(if (l > r) 1 else 0)
            ">=" -> CInt(if (l >= r) 1 else 0)
            "&" -> CInt(l and r)
            "|" -> CInt(l or r)
            "^" -> CInt(l xor r)
            "<<" -> CInt(l shl r)
            ">>" -> CInt(l shr r)
            else -> CInt(0)
        }
    }

    private fun evaluateUnary(expr: UnaryExpr): RuntimeValue {
        if (expr.op == "&") {
            // Address of operator
            return when (val op = expr.operand) {
                is VariableExpr -> {
                    var addr: Int? = null
                    for (i in frameAddresses.size - 1 downTo 0) {
                        addr = frameAddresses[i][op.name]
                        if (addr != null) break
                    }
                    if (addr == null) {
                        addr = nextAddress
                        nextAddress += 4
                        frameAddresses.last()[op.name] = addr
                        variableAddresses[op.name] = addr
                        memory[addr] = getVariable(op.name, expr.line)
                    }
                    CPointer(addr, 0, "int")
                }
                is ArrayAccessExpr -> {
                    val arrVal = evaluateExpr(op.array)
                    val idx = evaluateExpr(op.index).asInt()
                    val baseAddr = if (op.array is VariableExpr) variableAddresses[op.array.name] ?: 0x1000 else 0x1000
                    CPointer(baseAddr, idx * 4, "int")
                }
                else -> throw RuntimeError(expr.line, "Cannot take address of rvalue")
            }
        }

        if (expr.op == "*") {
            // Dereference operator
            val ptr = evaluateExpr(expr.operand)
            return when (ptr) {
                is CPointer -> {
                    val effAddr = ptr.address + ptr.offset
                    memory[effAddr] ?: CInt(0)
                }
                is CArray -> {
                    if (ptr.elements.isNotEmpty()) ptr.elements[0] else CInt(0)
                }
                is CString -> {
                    if (ptr.text.isNotEmpty()) CChar(ptr.text[0].code) else CChar(0)
                }
                else -> throw RuntimeError(expr.line, "Invalid dereference of non-pointer type")
            }
        }

        if (expr.op == "++" || expr.op == "--") {
            val isInc = expr.op == "++"
            if (expr.operand is VariableExpr) {
                val oldVal = getVariable(expr.operand.name, expr.line)
                val newVal = when (oldVal) {
                    is CFloat -> CFloat(if (isInc) oldVal.value + 1.0 else oldVal.value - 1.0)
                    else -> CInt(if (isInc) oldVal.asInt() + 1 else oldVal.asInt() - 1)
                }
                setVariable(expr.operand.name, newVal)
                return if (expr.isPrefix) newVal else oldVal
            } else if (expr.operand is UnaryExpr && expr.operand.op == "*") {
                // (*ptr)++
                val ptr = evaluateExpr(expr.operand.operand) as? CPointer ?: throw RuntimeError(expr.line, "Cannot increment dereference of non-pointer")
                val effAddr = ptr.address + ptr.offset
                val oldVal = memory[effAddr] ?: CInt(0)
                val newVal = CInt(if (isInc) oldVal.asInt() + 1 else oldVal.asInt() - 1)
                memory[effAddr] = newVal
                return if (expr.isPrefix) newVal else oldVal
            }
        }

        val operandVal = evaluateExpr(expr.operand)
        return when (expr.op) {
            "-" -> if (operandVal is CFloat) CFloat(-operandVal.value) else CInt(-operandVal.asInt())
            "+" -> operandVal
            "!" -> CInt(if (operandVal.asBoolean()) 0 else 1)
            "~" -> CInt(operandVal.asInt().inv())
            else -> operandVal
        }
    }

    private fun evaluateAssign(expr: AssignExpr): RuntimeValue {
        val rvalue = evaluateExpr(expr.value)

        when (val target = expr.target) {
            is VariableExpr -> {
                val currentVal = getVariable(target.name, expr.line)
                val finalVal = computeAssignValue(expr.op, currentVal, rvalue, expr.line)
                setVariable(target.name, finalVal)
                return finalVal
            }
            is ArrayAccessExpr -> {
                val arr = evaluateExpr(target.array)
                val idx = evaluateExpr(target.index).asInt()
                if (arr is CArray) {
                    if (idx < 0 || idx >= arr.elements.size) {
                        // Expand if reasonable
                        while (arr.elements.size <= idx) {
                            arr.elements.add(CInt(0))
                        }
                    }
                    val currentVal = arr.elements[idx]
                    val finalVal = computeAssignValue(expr.op, currentVal, rvalue, expr.line)
                    arr.elements[idx] = finalVal
                    return finalVal
                } else if (arr is CString) {
                    if (idx >= 0 && idx < arr.text.length) {
                        val chars = arr.text.toCharArray()
                        chars[idx] = rvalue.asInt().toChar()
                        arr.text = String(chars)
                    }
                    return rvalue
                } else {
                    throw RuntimeError(expr.line, "Subscripted value is not an array")
                }
            }
            is UnaryExpr -> {
                if (target.op == "*") {
                    val ptr = evaluateExpr(target.operand)
                    if (ptr is CPointer) {
                        val effAddr = ptr.address + ptr.offset
                        val currentVal = memory[effAddr] ?: CInt(0)
                        val finalVal = computeAssignValue(expr.op, currentVal, rvalue, expr.line)
                        memory[effAddr] = finalVal
                        return finalVal
                    }
                }
                throw RuntimeError(expr.line, "Invalid assignment target")
            }
            is MemberAccessExpr -> {
                val structVal = evaluateExpr(target.target)
                if (structVal is CStruct) {
                    val currentVal = structVal.fields[target.member] ?: CInt(0)
                    val finalVal = computeAssignValue(expr.op, currentVal, rvalue, expr.line)
                    structVal.fields[target.member] = finalVal
                    return finalVal
                }
                throw RuntimeError(expr.line, "Request for member in something not a structure")
            }
            else -> throw RuntimeError(expr.line, "Invalid lvalue in assignment")
        }
    }

    private fun computeAssignValue(op: String, current: RuntimeValue, right: RuntimeValue, line: Int): RuntimeValue {
        return when (op) {
            "=" -> right
            "+=" -> if (current is CFloat || right is CFloat) CFloat(current.asFloat() + right.asFloat()) else CInt(current.asInt() + right.asInt())
            "-=" -> if (current is CFloat || right is CFloat) CFloat(current.asFloat() - right.asFloat()) else CInt(current.asInt() - right.asInt())
            "*=" -> if (current is CFloat || right is CFloat) CFloat(current.asFloat() * right.asFloat()) else CInt(current.asInt() * right.asInt())
            "/=" -> {
                val div = right.asFloat()
                if (div == 0.0) throw RuntimeError(line, "Division by zero in /=")
                if (current is CFloat || right is CFloat) CFloat(current.asFloat() / div) else CInt(current.asInt() / right.asInt())
            }
            "%=" -> {
                val div = right.asInt()
                if (div == 0) throw RuntimeError(line, "Modulo by zero in %=")
                CInt(current.asInt() % div)
            }
            else -> right
        }
    }

    private fun evaluateCall(expr: CallExpr): RuntimeValue {
        // Check for Standard Library Built-ins
        when (expr.callee) {
            "printf" -> return builtinPrintf(expr)
            "scanf" -> return builtinScanf(expr)
            "puts" -> {
                val str = expr.arguments.firstOrNull()?.let { evaluateExpr(it).toDisplayString() } ?: ""
                stdout.append(str).append('\n')
                return CInt(str.length + 1)
            }
            "putchar" -> {
                val ch = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asInt() } ?: 0
                stdout.append(ch.toChar())
                return CInt(ch)
            }
            "getchar" -> {
                val ch = if (inputScanner.hasNext()) {
                    inputScanner.next().firstOrNull()?.code ?: -1
                } else -1
                return CInt(ch)
            }
            "strlen" -> {
                val str = expr.arguments.firstOrNull()?.let { evaluateExpr(it) }
                val len = when (str) {
                    is CString -> str.text.length
                    is CArray -> str.elements.indexOfFirst { it.asInt() == 0 }.let { if (it >= 0) it else str.elements.size }
                    else -> 0
                }
                return CInt(len)
            }
            "strcpy" -> {
                if (expr.arguments.size >= 2) {
                    val dest = evaluateExpr(expr.arguments[0])
                    val src = evaluateExpr(expr.arguments[1])
                    if (dest is CString) {
                        dest.text = src.toDisplayString()
                    } else if (dest is CArray) {
                        dest.elements.clear()
                        src.toDisplayString().forEach { dest.elements.add(CChar(it.code)) }
                        dest.elements.add(CChar(0))
                    }
                    return dest
                }
                return CInt(0)
            }
            "strcmp" -> {
                if (expr.arguments.size >= 2) {
                    val s1 = evaluateExpr(expr.arguments[0]).toDisplayString()
                    val s2 = evaluateExpr(expr.arguments[1]).toDisplayString()
                    return CInt(s1.compareTo(s2))
                }
                return CInt(0)
            }
            "strcat" -> {
                if (expr.arguments.size >= 2) {
                    val dest = evaluateExpr(expr.arguments[0])
                    val src = evaluateExpr(expr.arguments[1]).toDisplayString()
                    if (dest is CString) {
                        dest.text += src
                    }
                    return dest
                }
                return CInt(0)
            }
            "sqrt" -> {
                val x = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asFloat() } ?: 0.0
                return CFloat(sqrt(x))
            }
            "pow" -> {
                val b = expr.arguments.getOrNull(0)?.let { evaluateExpr(it).asFloat() } ?: 0.0
                val e = expr.arguments.getOrNull(1)?.let { evaluateExpr(it).asFloat() } ?: 0.0
                return CFloat(b.pow(e))
            }
            "abs" -> {
                val x = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asInt() } ?: 0
                return CInt(abs(x))
            }
            "floor" -> {
                val x = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asFloat() } ?: 0.0
                return CFloat(floor(x))
            }
            "ceil" -> {
                val x = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asFloat() } ?: 0.0
                return CFloat(ceil(x))
            }
            "sin" -> {
                val x = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asFloat() } ?: 0.0
                return CFloat(sin(x))
            }
            "cos" -> {
                val x = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asFloat() } ?: 0.0
                return CFloat(cos(x))
            }
            "rand" -> return CInt((0..32767).random())
            "srand" -> return CVoid
            "malloc" -> {
                val size = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asInt() } ?: 4
                val addr = nextAddress
                nextAddress += size
                return CPointer(addr, 0, "void")
            }
            "free" -> return CVoid
            "exit" -> {
                val code = expr.arguments.firstOrNull()?.let { evaluateExpr(it).asInt() } ?: 0
                throw ReturnException(CInt(code))
            }
        }

        // Custom User Function
        val func = functions[expr.callee] ?: throw RuntimeError(expr.line, "Implicit declaration of function '${expr.callee}'")
        val evaluatedArgs = expr.arguments.map { evaluateExpr(it) }
        return executeFunction(func, evaluatedArgs)
    }

    private fun builtinPrintf(expr: CallExpr): RuntimeValue {
        if (expr.arguments.isEmpty()) return CInt(0)

        val formatArg = evaluateExpr(expr.arguments[0])
        val formatStr = formatArg.toDisplayString()
        val otherArgs = expr.arguments.drop(1).map { evaluateExpr(it) }

        var argIdx = 0
        var i = 0
        val sb = StringBuilder()

        while (i < formatStr.length) {
            val c = formatStr[i]
            if (c == '%' && i + 1 < formatStr.length) {
                if (formatStr[i + 1] == '%') {
                    sb.append('%')
                    i += 2
                    continue
                }

                // Parse format specifier: e.g. %d, %5d, %.2f, %s, %c, %x, %p
                i++ // Skip '%'
                var precision = -1
                if (i < formatStr.length && formatStr[i] == '.') {
                    i++
                    var precStr = ""
                    while (i < formatStr.length && formatStr[i].isDigit()) {
                        precStr += formatStr[i++]
                    }
                    precision = precStr.toIntOrNull() ?: -1
                }

                if (i < formatStr.length && (formatStr[i] == 'l' || formatStr[i] == 'h')) {
                    i++ // skip length modifiers like ld, lf
                }

                if (i < formatStr.length) {
                    val spec = formatStr[i].toString()
                    val value = if (argIdx < otherArgs.size) otherArgs[argIdx++] else CInt(0)
                    sb.append(value.toFormatted(spec, 0, precision))
                    i++
                }
            } else {
                sb.append(c)
                i++
            }
        }

        val out = sb.toString()
        stdout.append(out)
        return CInt(out.length)
    }

    private fun builtinScanf(expr: CallExpr): RuntimeValue {
        if (expr.arguments.size < 2) return CInt(0)

        val formatStr = evaluateExpr(expr.arguments[0]).toDisplayString()
        val targets = expr.arguments.drop(1)
        var matched = 0

        val specifiers = mutableListOf<String>()
        var i = 0
        while (i < formatStr.length) {
            if (formatStr[i] == '%' && i + 1 < formatStr.length) {
                i++
                if (formatStr[i] == 'l' && i + 1 < formatStr.length) i++
                specifiers.add(formatStr[i].toString())
            }
            i++
        }

        for (k in targets.indices) {
            if (k >= specifiers.size) break
            if (!inputScanner.hasNext()) break

            val spec = specifiers[k]
            val targetArg = targets[k]

            // targetArg is typically &var or arr
            val ptr = when (targetArg) {
                is UnaryExpr -> if (targetArg.op == "&") evaluateExpr(targetArg) else null
                is VariableExpr -> {
                    // Array decaying to pointer
                    val addr = variableAddresses[targetArg.name] ?: 0x1000
                    CPointer(addr, 0, "char")
                }
                else -> null
            }

            when (spec) {
                "d", "i" -> {
                    if (inputScanner.hasNextInt()) {
                        val num = inputScanner.nextInt()
                        assignScanfTarget(targetArg, ptr, CInt(num))
                        matched++
                    } else if (inputScanner.hasNext()) {
                        val s = inputScanner.next()
                        val num = s.toIntOrNull() ?: 0
                        assignScanfTarget(targetArg, ptr, CInt(num))
                        matched++
                    }
                }
                "f", "lf" -> {
                    if (inputScanner.hasNextDouble()) {
                        val f = inputScanner.nextDouble()
                        assignScanfTarget(targetArg, ptr, CFloat(f))
                        matched++
                    } else if (inputScanner.hasNext()) {
                        val s = inputScanner.next()
                        val f = s.toDoubleOrNull() ?: 0.0
                        assignScanfTarget(targetArg, ptr, CFloat(f))
                        matched++
                    }
                }
                "s" -> {
                    if (inputScanner.hasNext()) {
                        val s = inputScanner.next()
                        assignScanfTarget(targetArg, ptr, CString(s))
                        matched++
                    }
                }
                "c" -> {
                    if (inputScanner.hasNext()) {
                        val s = inputScanner.next()
                        val ch = s.firstOrNull()?.code ?: 0
                        assignScanfTarget(targetArg, ptr, CChar(ch))
                        matched++
                    }
                }
            }
        }

        return CInt(matched)
    }

    private fun assignScanfTarget(targetExpr: ExprNode, ptr: RuntimeValue?, value: RuntimeValue) {
        if (targetExpr is UnaryExpr && targetExpr.op == "&" && targetExpr.operand is VariableExpr) {
            val varName = targetExpr.operand.name
            setVariable(varName, value)
            return
        }

        if (targetExpr is VariableExpr) {
            setVariable(targetExpr.name, value)
            return
        }

        if (ptr is CPointer) {
            val effAddr = ptr.address + ptr.offset
            memory[effAddr] = value
        }
    }

    private fun evaluateArrayAccess(expr: ArrayAccessExpr): RuntimeValue {
        val arr = evaluateExpr(expr.array)
        val idx = evaluateExpr(expr.index).asInt()

        return when (arr) {
            is CArray -> {
                if (idx < 0 || idx >= arr.elements.size) {
                    throw RuntimeError(expr.line, "Array index $idx out of bounds for size ${arr.elements.size}")
                }
                arr.elements[idx]
            }
            is CString -> {
                if (idx < 0 || idx >= arr.text.length) {
                    CChar(0)
                } else {
                    CChar(arr.text[idx].code)
                }
            }
            is CPointer -> {
                val effAddr = arr.address + (arr.offset + idx * 4)
                memory[effAddr] ?: CInt(0)
            }
            else -> throw RuntimeError(expr.line, "Subscripted value is neither array nor pointer")
        }
    }

    private fun evaluateMemberAccess(expr: MemberAccessExpr): RuntimeValue {
        val target = evaluateExpr(expr.target)
        val structVal = if (expr.isArrow && target is CPointer) {
            memory[target.address + target.offset] as? CStruct
        } else {
            target as? CStruct
        } ?: throw RuntimeError(expr.line, "Request for member '${expr.member}' in something not a structure")

        return structVal.fields[expr.member] ?: throw RuntimeError(expr.line, "Struct '${structVal.structName}' has no member named '${expr.member}'")
    }

    private fun getVariable(name: String, line: Int): RuntimeValue {
        // Search stack frames from innermost to global
        for (i in callStack.size - 1 downTo 0) {
            val frame = callStack[i]
            if (frame.containsKey(name)) {
                val addr = frameAddresses[i][name]
                if (addr != null && memory.containsKey(addr)) {
                    return memory[addr]!!
                }
                return frame[name]!!
            }
        }

        val addr = variableAddresses[name]
        if (addr != null && memory.containsKey(addr)) {
            return memory[addr]!!
        }

        throw RuntimeError(line, "Use of undeclared identifier '$name'")
    }

    private fun setVariable(name: String, value: RuntimeValue, isDecl: Boolean = false) {
        if (isDecl) {
            callStack.last()[name] = value
            val addr = nextAddress
            nextAddress += 4
            memory[addr] = value
            frameAddresses.last()[name] = addr
            variableAddresses[name] = addr
            return
        }

        for (i in callStack.size - 1 downTo 0) {
            val frame = callStack[i]
            if (frame.containsKey(name)) {
                frame[name] = value
                val addr = frameAddresses[i][name]
                if (addr != null) memory[addr] = value
                return
            }
        }

        // Default to current scope
        callStack.last()[name] = value
        val addr = nextAddress
        nextAddress += 4
        memory[addr] = value
        frameAddresses.last()[name] = addr
        variableAddresses[name] = addr
    }

    private fun checkInstructions(line: Int) {
        instructionCount++
        if (instructionCount > MAX_INSTRUCTIONS) {
            throw RuntimeError(line, "Potential infinite loop detected! Execution terminated after exceeding instruction threshold ($MAX_INSTRUCTIONS instructions).")
        }
    }

    private fun recordDebugSnapshot(line: Int, description: String) {
        if (debugSnapshots.size > 150) return // Cap snapshots for memory safety
        val currentFrame = callStack.lastOrNull() ?: return
        val vars = currentFrame.map { (k, v) ->
            val addrStr = variableAddresses[k]?.let { "0x%04X".format(it) } ?: "stack"
            DebugVariable(k, v::class.simpleName?.removePrefix("C") ?: "val", v.toDisplayString(), addrStr)
        }
        debugSnapshots.add(
            DebugSnapshot(
                stepIndex = debugSnapshots.size + 1,
                line = line,
                description = description,
                callStack = stackFrameNames.toList(),
                variables = vars
            )
        )
    }
}
