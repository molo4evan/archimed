package utils

import ir.types.Type
import java.util.*

object SymbolTable {
    private val SYMBOL_STACK = Stack<MutableMap<Type, MutableList<Symbol>>>()

    init {
        SYMBOL_STACK.push(mutableMapOf())
    }

    fun add(symbol: Symbol){
        val symbols = SYMBOL_STACK.peek()
        if (!symbols.containsKey(symbol.type)){
            symbols[symbol.type] = mutableListOf()
        }
            symbols[symbol.type]!!.add(symbol)
    }

    fun remove(symbol: Symbol){
        val symbols = SYMBOL_STACK.peek()
        if (symbols.containsKey(symbol.type)){
            val symbolsOfType = symbols[symbol.type]
            symbolsOfType!!.remove(symbol)
            if (symbolsOfType.isEmpty()) {
                symbols.remove(symbol.type)
            }
        }
    }

    fun contains(name: String): Boolean {
        val symbols = SYMBOL_STACK.peek()
        val names = mutableListOf<String>()
        for (symbolsOfType in symbols) {
            for (symbol in symbolsOfType.value) {
                names.add(symbol.name)
            }
        }
        return names.contains(name)
    }

    fun contains(symbol: Symbol): Boolean {
        val symbols = SYMBOL_STACK.peek()
        return symbols[symbol.type]?.contains(symbol) ?: false
    }

    fun get(type: Type): List<Symbol>{
        val symbols = SYMBOL_STACK.peek()
        return if (symbols.containsKey(type)) symbols[type]!!
        else emptyList()
    }

    fun get(name: String): Symbol? {
        val symbols = SYMBOL_STACK.peek()
        for (typeSymbols in symbols){
            val out = typeSymbols.value.find { it.name == name }
            if (out != null) return out
        }
        return null
    }

    fun getAll() = HashMap<Type, List<Symbol>>(SYMBOL_STACK.peek())

    fun removeAll() {
        SYMBOL_STACK.clear()
        SYMBOL_STACK.push(mutableMapOf())
    }

    fun push() {
        // Do deep cloning..
        val prev = SYMBOL_STACK.peek()
        val top = mutableMapOf<Type, MutableList<Symbol>>()
        SYMBOL_STACK.push(top)
        for ((key, prevArray) in prev) {
            val topArray = mutableListOf<Symbol>()
            top[key] = topArray
            for (symbol in prevArray) {
                topArray.add(symbol.copy())
            }
        }
    }

    fun merge() {
        // Merging means moving element at the top of stack one step down, while removing the
        // previous element.
        val top = SYMBOL_STACK.pop()
        SYMBOL_STACK.pop()
        SYMBOL_STACK.push(top)
    }

    fun pop() {
        SYMBOL_STACK.pop()
    }
}