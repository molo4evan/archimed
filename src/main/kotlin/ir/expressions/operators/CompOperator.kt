package ir.expressions.operators

import exceptions.ParserException
import ir.expressions.Expression
import ir.types.TypeBoolean

class CompOperator(val kind: OpKind, left: Expression, right: Expression): BinaryOperator(TypeBoolean, left, right) {
    enum class OpKind{
        EQ,
        NEQ,
        LESS,
        MORE,
        LESS_EQ,
        MORE_EQ
    }

    init {
        if (left.returnType != right.returnType) throw ParserException("Different comparsion operand types")
    }
}