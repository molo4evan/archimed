package ir.statements

import exceptions.ParserException
import ir.Block
import ir.expressions.Expression
import ir.types.TypeBoolean

class IfNode(
    val condition: Expression,
    val thenBranch: Block,
    val elseBranch: Block?
): Statement() {
    init {
        if (condition.returnType != TypeBoolean) throw ParserException("IncorrectType")
    }
}