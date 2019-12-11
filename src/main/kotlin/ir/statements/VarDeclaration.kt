package ir.statements

import exceptions.ParserException
import ir.IRNode
import ir.types.Type
import utils.SymbolTable
import utils.VariableInfo

class VarDeclaration(val info: VariableInfo): Statement() {
    init {
        if (SymbolTable.contains(info.name))
            throw ParserException("Variable redefinition")
        SymbolTable.add(info)
    }
}