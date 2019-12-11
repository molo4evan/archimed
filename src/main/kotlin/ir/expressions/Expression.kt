package ir.expressions

import ir.IRNode
import ir.types.Type

abstract class Expression(returnType: Type): IRNode(returnType)