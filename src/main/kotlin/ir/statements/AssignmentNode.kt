package ir.statements

import ir.IRNode
import ir.expressions.Expression
import jdk.nashorn.internal.runtime.ParserException
import utils.SymbolTable
import utils.VariableInfo

class AssignmentNode(val identifier: VariableInfo, val expr: Expression): Statement() {
    init {
        if (!SymbolTable.contains(identifier)) throw ParserException("Assignment to nonexistent variable")
        if (identifier.type != expr.returnType) throw ParserException("Invalid initializing expression type")
    }
}