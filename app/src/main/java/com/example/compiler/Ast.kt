package com.example.compiler

sealed class AstNode {
    var line: Int = 0
}

data class CType(
    val baseType: String, // "int", "float", "char", "double", "void", "struct"
    val pointerLevel: Int = 0,
    val arrayDimensions: List<Int> = emptyList(),
    val structName: String? = null
) {
    fun isPointer(): Boolean = pointerLevel > 0 || arrayDimensions.isNotEmpty()
    override fun toString(): String {
        val ptrStr = "*".repeat(pointerLevel)
        val arrStr = arrayDimensions.joinToString("") { "[$it]" }
        val base = if (structName != null) "struct $structName" else baseType
        return "$base$ptrStr$arrStr"
    }
}

// Expressions
sealed class ExprNode : AstNode()

data class IntLiteralExpr(val value: Int) : ExprNode()
data class FloatLiteralExpr(val value: Double) : ExprNode()
data class CharLiteralExpr(val value: Int) : ExprNode()
data class StringLiteralExpr(val value: String) : ExprNode()

data class VariableExpr(val name: String) : ExprNode()

data class BinaryExpr(
    val left: ExprNode,
    val op: String,
    val right: ExprNode
) : ExprNode()

data class UnaryExpr(
    val op: String,
    val operand: ExprNode,
    val isPrefix: Boolean = true
) : ExprNode()

data class AssignExpr(
    val target: ExprNode,
    val op: String, // "=", "+=", "-=", etc.
    val value: ExprNode
) : ExprNode()

data class CallExpr(
    val callee: String,
    val arguments: List<ExprNode>
) : ExprNode()

data class ArrayAccessExpr(
    val array: ExprNode,
    val index: ExprNode
) : ExprNode()

data class MemberAccessExpr(
    val target: ExprNode,
    val member: String,
    val isArrow: Boolean = false
) : ExprNode()

data class TernaryExpr(
    val condition: ExprNode,
    val thenBranch: ExprNode,
    val elseBranch: ExprNode
) : ExprNode()

data class SizeofExpr(
    val targetType: CType?,
    val targetExpr: ExprNode?
) : ExprNode()

data class CastExpr(
    val targetType: CType,
    val expr: ExprNode
) : ExprNode()

// Statements
sealed class StmtNode : AstNode()

data class ExprStmt(val expr: ExprNode) : StmtNode()

data class VarDeclStmt(
    val type: CType,
    val name: String,
    val initializer: ExprNode? = null,
    val arrayInitializer: List<ExprNode>? = null
) : StmtNode()

data class BlockStmt(val statements: List<StmtNode>) : StmtNode()

data class IfStmt(
    val condition: ExprNode,
    val thenBranch: StmtNode,
    val elseBranch: StmtNode? = null
) : StmtNode()

data class WhileStmt(
    val condition: ExprNode,
    val body: StmtNode
) : StmtNode()

data class DoWhileStmt(
    val body: StmtNode,
    val condition: ExprNode
) : StmtNode()

data class ForStmt(
    val init: StmtNode?,
    val condition: ExprNode?,
    val step: ExprNode?,
    val body: StmtNode
) : StmtNode()

data class SwitchStmt(
    val condition: ExprNode,
    val body: StmtNode
) : StmtNode()

data class CaseStmt(val value: Int?, val isDefault: Boolean = false) : StmtNode()

data class ReturnStmt(val expr: ExprNode?) : StmtNode()
class BreakStmt : StmtNode()
class ContinueStmt : StmtNode()

// Declarations
data class Parameter(
    val type: CType,
    val name: String
)

data class FunctionDef(
    val returnType: CType,
    val name: String,
    val parameters: List<Parameter>,
    val body: BlockStmt
) : AstNode()

data class StructDef(
    val name: String,
    val members: List<Pair<CType, String>>
) : AstNode()

data class Program(
    val structs: List<StructDef>,
    val globals: List<VarDeclStmt>,
    val functions: List<FunctionDef>,
    val includes: List<String>,
    val defines: Map<String, String>
) : AstNode()
