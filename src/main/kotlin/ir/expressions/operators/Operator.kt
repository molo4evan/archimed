package ir.expressions.operators

import ir.expressions.Expression
import ir.types.Type

abstract class Operator(resultType: Type): Expression(resultType)