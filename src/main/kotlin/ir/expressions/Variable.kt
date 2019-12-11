package ir.expressions

import exceptions.ParserException
import utils.SymbolTable
import utils.VariableInfo

class Variable(val varInfo: VariableInfo): Expression(varInfo.type) {
    init {
        if (!SymbolTable.contains(varInfo)) throw ParserException("Undefined variable")
    }
}