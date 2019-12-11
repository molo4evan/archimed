package ir.expressions.operators

import exceptions.ParserException
import ir.expressions.Expression
import ir.types.TypeBoolean

class LogicBinaryOperator(
    val kind: OpKind,
    left: Expression,
    right: Expression
): BinaryOperator(TypeBoolean, left, right) {
    enum class OpKind {
        AND,
        OR
    }

    init {
        if (left.returnType != TypeBoolean || right.returnType != TypeBoolean)
            throw ParserException("Invalid operand type")
    }
}