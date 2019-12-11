package ir.expressions.literals

import ir.IRNode
import ir.types.TypeString

class StringLiteral(value: String): Literal<String>(TypeString, value) {
}