package ir.statements

import exceptions.ParserException
import ir.Block
import ir.expressions.Expression
import ir.types.TypeBoolean

class WhileNode(
    val condition: Expression,
    val body: Block
): Statement() {
    init {
        if (condition.returnType != TypeBoolean) throw ParserException("IncorrectType")
    }
}