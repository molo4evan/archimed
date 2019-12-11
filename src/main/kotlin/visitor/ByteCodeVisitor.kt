package visitor

import ir.Block
import ir.MainNode
import ir.expressions.Expression
import ir.expressions.Variable
import ir.expressions.literals.IntLiteral
import ir.expressions.literals.StringLiteral
import ir.expressions.operators.*
import ir.expressions.operators.CompOperator.OpKind.*
import ir.expressions.operators.LogicBinaryOperator.OpKind.*
import ir.expressions.operators.LogicUnaryOperator.OpKind.*
import ir.expressions.operators.MathBinaryOperator.OpKind.*
import ir.expressions.operators.MathUnaryOperator.OpKind.*
import ir.statements.*
import ir.types.TypeInt
import ir.types.TypeString
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import jdk.internal.org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import jdk.internal.org.objectweb.asm.Opcodes.*
import jdk.internal.org.objectweb.asm.tree.*
import parser.Parser
import utils.VariableInfo
import java.util.*

class ByteCodeVisitor: Visitor {
    private lateinit var instructions: InsnList
    private val cn = ClassNode()

    private val variables = mutableMapOf<VariableInfo, Int>()
    private val indexStack = Stack<Int>()

    private var className = "Main"

    init {
        cn.access = ACC_SUPER
        cn.name = "Main"
        cn.superName = "java/lang/Object"
        cn.version = 52
        reset()
    }

    fun setClassName(name: String){
        className = name
        cn.name = className
    }

    private fun pushVarStack() {
        indexStack.push(indexStack.peek())
    }

    private fun popVarStack() = indexStack.pop()

    private fun reset(){
        val main = MethodNode(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        cn.methods.clear()
        addConstructor()
        cn.methods.add(main)
        instructions = main.instructions
        variables.clear()
        indexStack.clear()
        indexStack.push(1)
    }

    private fun addConstructor() {
        val constr = MethodNode(0, "<init>", "()V", null, null)
        val insns = constr.instructions
        insns.add(VarInsnNode(ALOAD, 0))
        insns.add(MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false))
        insns.add(InsnNode(RETURN))
        cn.methods.add(constr)
    }

    override fun getResult(): ByteArray {
        val cw = ClassWriter(COMPUTE_MAXS or COMPUTE_FRAMES)
        cn.accept(cw)
        return cw.toByteArray()
    }

    override fun visit(node: MainNode) {
        reset()
        visit(node as Block)
        instructions.add(InsnNode(RETURN))
    }

    override fun visit(node: Block) {
        for (statement in node.statements) {
            visit(statement)
        }
    }

    override fun visit(node: Statement) {
        when (node) {
            is IfNode -> visit(node)
            is WhileNode -> visit(node)
            is PrintNode -> visit(node)
            is AssignmentNode -> visit(node)
            is VarDeclaration -> visit(node)
            is VarInitialization -> visit(node)
            else -> throw Exception()
        }
    }

    override fun visit(node: IfNode) {
        visit(node.condition)
        val elseLabel = LabelNode()
        val endLabel = LabelNode()
        instructions.add(JumpInsnNode(
            IFEQ,
            if (node.elseBranch != null) elseLabel else endLabel
        ))
        pushVarStack()
        visit(node.thenBranch)
        popVarStack()
        if (node.elseBranch != null){
            instructions.add(JumpInsnNode(GOTO, endLabel))
            instructions.add(elseLabel)
            pushVarStack()
            visit(node.elseBranch)
            popVarStack()
        }
        instructions.add(endLabel)
    }

    override fun visit(node: WhileNode) {
        val start = LabelNode()
        instructions.add(start)
        visit(node.condition)
        val out = LabelNode()
        instructions.add(JumpInsnNode(IFEQ, out))
        pushVarStack()
        visit(node.body)
        popVarStack()
        instructions.add(JumpInsnNode(GOTO, start))
        instructions.add(out)
    }

    override fun visit(node: PrintNode) {
        println()
        instructions.add(FieldInsnNode(
            GETSTATIC,
            "java/lang/System",
            "out",
            "Ljava/io/PrintStream;"
        ))
        visit(node.expr)
        instructions.add(MethodInsnNode(
            INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(${node.expr.returnType})V",
            false
        ))
    }

    override fun visit(node: AssignmentNode) {
        val index = variables[node.identifier] ?: throw Exception()
        visit(node.expr)
        val opcode = when (node.expr.returnType) {
            TypeInt -> {
                ISTORE
            }
            TypeString -> {
                ASTORE
            }
            else -> throw Exception()
        }
        instructions.add(VarInsnNode(opcode, index))
    }

    override fun visit(node: VarDeclaration) {
        val curFreeIndex = indexStack.pop()
        indexStack.push(curFreeIndex + 1)
        variables[node.info] = curFreeIndex
    }

    override fun visit(node: VarInitialization) {
        val varIndex = indexStack.pop()
        indexStack.push(varIndex + 1)
        variables[node.info] = varIndex
        visit(node.initExpr)
        val opcode = when (node.initExpr.returnType) {
            TypeInt -> {
                ISTORE
            }
            TypeString -> {
                ASTORE
            }
            else -> throw Exception()
        }
        instructions.add(VarInsnNode(opcode, varIndex))
    }

    override fun visit(node: Expression) {
        when (node) {
            is IntLiteral -> visit(node)
            is StringLiteral -> visit(node)
            is CompOperator -> visit(node)
            is LogicBinaryOperator -> visit(node)
            is LogicUnaryOperator -> visit(node)
            is MathBinaryOperator -> visit(node)
            is MathUnaryOperator -> visit(node)
            is Variable -> visit(node)
            else -> throw Exception()
        }
    }

    override fun visit(node: CompOperator) {
        visit(node.leftOperand)
        visit(node.rightOperand)
        val label1 = LabelNode()
        val label2 = LabelNode()
        var opcode = when (node.kind) {
            EQ -> IF_ICMPEQ
            NEQ -> IF_ICMPNE
            LESS -> IF_ICMPLT
            MORE -> IF_ICMPGT
            LESS_EQ -> IF_ICMPLE
            MORE_EQ -> IF_ICMPGE
        }
        if (node.leftOperand.returnType == TypeString || node.rightOperand.returnType == TypeString){
            opcode = when (opcode) {
                IF_ICMPEQ -> IF_ACMPEQ
                IF_ICMPNE -> IF_ACMPNE
                else -> throw Exception("Unexpected string comparsion type")
            }
        }
        instructions.add(JumpInsnNode(opcode, label1))
        instructions.add(InsnNode(ICONST_0))
        instructions.add(JumpInsnNode(GOTO, label2))
        instructions.add(label1)
        instructions.add(InsnNode(ICONST_1))
        instructions.add(label2)
    }

    override fun visit(node: LogicBinaryOperator) {
        visit(node.leftOperand)
        when (node.kind){
            AND -> {
                val label1 = LabelNode()
                val label2 = LabelNode()
                instructions.add(JumpInsnNode(IFEQ, label1))
                visit(node.rightOperand)
                instructions.add(JumpInsnNode(IFEQ, label1))
                instructions.add(InsnNode(ICONST_1))
                instructions.add(JumpInsnNode(GOTO, label2))
                instructions.add(label1)
                instructions.add(InsnNode(ICONST_0))
                instructions.add(label2)
            }
            OR -> {
                val label1 = LabelNode()
                val label2 = LabelNode()
                val label3 = LabelNode()
                instructions.add(JumpInsnNode(IFNE, label1))
                visit(node.rightOperand)
                instructions.add(JumpInsnNode(IFEQ, label2))
                instructions.add(label1)
                instructions.add(InsnNode(ICONST_1))
                instructions.add(JumpInsnNode(GOTO, label3))
                instructions.add(label2)
                instructions.add(InsnNode(ICONST_0))
                instructions.add(label3)
            }
        }
    }

    override fun visit(node: LogicUnaryOperator) {
        visit(node.operand)
        when (node.kind) {
            NEG -> {
                val labelElse = LabelNode()
                val labelEnd = LabelNode()
                instructions.add(JumpInsnNode(IFNE, labelElse))
                instructions.add(InsnNode(ICONST_1))
                instructions.add(JumpInsnNode(GOTO, labelEnd))
                instructions.add(labelElse)
                instructions.add(InsnNode(ICONST_0))
                instructions.add(labelEnd)
            }
        }
    }

    override fun visit(node: MathBinaryOperator) {
        visit(node.leftOperand)
        visit(node.rightOperand)
        instructions.add(InsnNode(
            when (node.kind) {
                ADD -> IADD
                SUB -> ISUB
                MULT -> IMUL
                DIV -> IDIV
            }
        ))
    }

    override fun visit(node: MathUnaryOperator) {
        visit(node.operand)
        instructions.add(InsnNode(
            when (node.kind) {
                MINUS -> INEG
            }
        ))
    }

    override fun visit(node: IntLiteral) {
        instructions.add(LdcInsnNode(node.value))
    }

    override fun visit(node: StringLiteral) {
        instructions.add(LdcInsnNode(node.value))
    }

    override fun visit(node: Variable) {
        val index = variables[node.varInfo] ?: throw Exception()
        val opcode = when (node.varInfo.type) {
            TypeInt -> {
                ILOAD
            }
            TypeString -> {
                ALOAD
            }
            else -> throw Exception()
        }
        instructions.add(VarInsnNode(opcode, index))
    }
}