package ir.expressions.operators

import exceptions.ParserException
import ir.expressions.Expression
import ir.types.TypeInt

class MathBinaryOperator(val kind: OpKind, left: Expression, right: Expression): BinaryOperator(TypeInt, left, right) {
    enum class OpKind {
        ADD,
        SUB,
        MULT,
        DIV
    }

    init {
        if (left.returnType != TypeInt || right.returnType != TypeInt)
            throw ParserException("Invalid math operand type")
    }
}