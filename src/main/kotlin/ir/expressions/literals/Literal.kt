package ir.expressions.literals

import ir.expressions.Expression
import ir.IRNode
import ir.types.Type

abstract class Literal<T>(type: Type, val value: T): Expression(type) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Literal<*>
        return value == other.value
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}