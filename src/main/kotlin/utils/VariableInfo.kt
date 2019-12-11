package utils

import ir.types.Type

class VariableInfo(name: String, type: Type): Symbol(name, type) {
    private var initialized = false

    constructor(other: VariableInfo): this(other.name, other.type)

    fun isInitialized() = initialized

    fun initialize(){
        initialized = true
    }

    override fun copy() = VariableInfo(this)
}