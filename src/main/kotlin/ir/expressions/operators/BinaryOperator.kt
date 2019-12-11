package ir.expressions.operators

import ir.expressions.Expression
import ir.types.Type

abstract class BinaryOperator(
    resultType: Type,
    val leftOperand: Expression,
    val rightOperand: Expression
): Operator(resultType)