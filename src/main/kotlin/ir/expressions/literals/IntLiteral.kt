package ir.expressions.literals

import ir.IRNode
import ir.types.TypeInt

class IntLiteral(value: Int): Literal<Int>(TypeInt, value) {
}