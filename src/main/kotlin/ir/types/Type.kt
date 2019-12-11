package ir.types

import ir.IRNode

open class Type(val name: String): IRNode(), Comparable<Type> {
    override fun equals(other: Any?) = this === other ||
                (other !== null && other is Type && this.name == other.name)

    override fun compareTo(other: Type) = name.compareTo(other.name)

    override fun hashCode() = name.hashCode()
}