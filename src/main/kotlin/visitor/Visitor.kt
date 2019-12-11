package visitor

import ir.*
import ir.expressions.Expression
import ir.expressions.Variable
import ir.expressions.literals.*
import ir.expressions.operators.*
import ir.statements.*

interface Visitor {
    fun getResult(): ByteArray

    fun visit(node: MainNode)
    fun visit(node: Block)
    fun visit(node: Statement)
    fun visit(node: IfNode)
    fun visit(node: WhileNode)
    fun visit(node: PrintNode)
    fun visit(node: AssignmentNode)
    fun visit(node: VarDeclaration)
    fun visit(node: VarInitialization)
    fun visit(node: Expression)
    fun visit(node: CompOperator)
    fun visit(node: LogicBinaryOperator)
    fun visit(node: LogicUnaryOperator)
    fun visit(node: MathBinaryOperator)
    fun visit(node: MathUnaryOperator)
    fun visit(node: IntLiteral)
    fun visit(node: StringLiteral)
    fun visit(node: Variable)
}