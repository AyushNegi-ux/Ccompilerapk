package com.example.compiler

sealed class RuntimeValue {
    abstract fun asInt(): Int
    abstract fun asFloat(): Double
    abstract fun asBoolean(): Boolean
    abstract fun toDisplayString(): String

    open fun toFormatted(specifier: String, width: Int = 0, precision: Int = -1): String {
        return toDisplayString()
    }
}

data class CInt(val value: Int) : RuntimeValue() {
    override fun asInt(): Int = value
    override fun asFloat(): Double = value.toDouble()
    override fun asBoolean(): Boolean = value != 0
    override fun toDisplayString(): String = value.toString()
    override fun toFormatted(specifier: String, width: Int, precision: Int): String {
        return when (specifier) {
            "x", "X" -> if (specifier == "X") Integer.toHexString(value).uppercase() else Integer.toHexString(value)
            "c" -> (value.toChar()).toString()
            else -> value.toString()
        }
    }
}

data class CFloat(val value: Double) : RuntimeValue() {
    override fun asInt(): Int = value.toInt()
    override fun asFloat(): Double = value
    override fun asBoolean(): Boolean = value != 0.0
    override fun toDisplayString(): String = "%.6f".format(java.util.Locale.US, value).trimEnd('0').let { if (it.endsWith(".")) "${it}0" else it }
    override fun toFormatted(specifier: String, width: Int, precision: Int): String {
        return if (precision >= 0) {
            "%.${precision}f".format(java.util.Locale.US, value)
        } else {
            "%.6f".format(java.util.Locale.US, value)
        }
    }
}

data class CChar(val value: Int) : RuntimeValue() {
    override fun asInt(): Int = value
    override fun asFloat(): Double = value.toDouble()
    override fun asBoolean(): Boolean = value != 0
    override fun toDisplayString(): String = (value.toChar()).toString()
    override fun toFormatted(specifier: String, width: Int, precision: Int): String {
        return when (specifier) {
            "d", "i" -> value.toString()
            else -> (value.toChar()).toString()
        }
    }
}

data class CString(var text: String) : RuntimeValue() {
    override fun asInt(): Int = text.hashCode()
    override fun asFloat(): Double = 0.0
    override fun asBoolean(): Boolean = text.isNotEmpty()
    override fun toDisplayString(): String = text
    override fun toFormatted(specifier: String, width: Int, precision: Int): String = text
}

data class CArray(
    val elements: MutableList<RuntimeValue>,
    val elementType: String
) : RuntimeValue() {
    override fun asInt(): Int = elements.size
    override fun asFloat(): Double = elements.size.toDouble()
    override fun asBoolean(): Boolean = elements.isNotEmpty()
    override fun toDisplayString(): String = elements.joinToString(prefix = "{", postfix = "}") { it.toDisplayString() }
}

data class CPointer(
    val address: Int,
    val offset: Int = 0,
    val targetType: String = "int"
) : RuntimeValue() {
    override fun asInt(): Int = address + offset
    override fun asFloat(): Double = (address + offset).toDouble()
    override fun asBoolean(): Boolean = (address + offset) != 0
    override fun toDisplayString(): String = "0x%08X".format(address + offset)
    override fun toFormatted(specifier: String, width: Int, precision: Int): String {
        return if (specifier == "p") "0x%08x".format(address + offset) else (address + offset).toString()
    }
}

data class CStruct(
    val structName: String,
    val fields: MutableMap<String, RuntimeValue>
) : RuntimeValue() {
    override fun asInt(): Int = 0
    override fun asFloat(): Double = 0.0
    override fun asBoolean(): Boolean = true
    override fun toDisplayString(): String = "struct $structName { ${fields.entries.joinToString { "${it.key}: ${it.value.toDisplayString()}" }} }"
}

object CVoid : RuntimeValue() {
    override fun asInt(): Int = 0
    override fun asFloat(): Double = 0.0
    override fun asBoolean(): Boolean = false
    override fun toDisplayString(): String = "void"
}
