package ir.statements

import exceptions.ParserException
import ir.IRNode
import ir.expressions.Expression
import utils.SymbolTable
import utils.VariableInfo

class VarInitialization(val info: VariableInfo, val initExpr: Expression): Statement() {
    init {
        if (SymbolTable.contains(info.name)) throw ParserException("Variable redefinition")
        if (info.type != initExpr.returnType) throw ParserException("Invalid initializing expression type")
        SymbolTable.add(info)
    }
}