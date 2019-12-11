package ir.expressions.operators

import ir.expressions.Expression
import ir.types.TypeBoolean
import ir.types.TypeInt
import jdk.nashorn.internal.runtime.ParserException

class MathUnaryOperator(val kind: OpKind, operand: Expression): UnaryOperator(TypeInt, operand) {
    enum class OpKind {
        MINUS
    }

    init {
        if (operand.returnType !is TypeInt) throw ParserException("Invalid operand type")
    }
}