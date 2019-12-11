package ir

import ir.statements.Statement

open class Block(): IRNode() {
    val statements = mutableListOf<Statement>()
}