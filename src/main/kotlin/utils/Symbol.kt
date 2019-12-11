package utils

import ir.types.Type

open class Symbol(val name: String, val type: Type) {
    protected constructor(other: Symbol): this(other.name, other.type)

    override fun equals(other: Any?) = this === other ||
                (other !== null && other is Symbol &&
                        name == other.name && type == other.type)

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    open fun copy() = Symbol(this)
}