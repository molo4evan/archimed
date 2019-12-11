package ir.expressions.operators

import exceptions.ParserException
import ir.expressions.Expression
import ir.types.TypeBoolean

class LogicUnaryOperator(
    val kind: OpKind = OpKind.NEG,
    operand: Expression
): UnaryOperator(TypeBoolean, operand) {
    enum class OpKind{
        NEG
    }

    init {
        if (operand.returnType != TypeBoolean) throw ParserException("Invalid operand type")
    }
}