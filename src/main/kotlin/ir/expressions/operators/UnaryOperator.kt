package ir.expressions.operators

import ir.expressions.Expression
import ir.types.Type

abstract class UnaryOperator(
    resultType: Type,
    val operand: Expression
): Operator(resultType)