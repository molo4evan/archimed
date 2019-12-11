package parser

import exceptions.ParserException
import ir.*
import ir.expressions.Expression
import ir.expressions.Variable
import ir.expressions.literals.IntLiteral
import ir.expressions.literals.StringLiteral
import ir.expressions.operators.*
import ir.statements.*
import ir.types.TypeBoolean
import ir.types.TypeInt
import ir.types.TypeString
import lexer.*
import lexer.If
import lexer.Print
import utils.SymbolTable
import utils.VariableInfo

class Parser(private val lexer: Lexer) {
    private var current = lexer.getLexeme()

    private fun getNext() {
        current = lexer.getLexeme()
    }

    fun parseMain(): MainNode {
        return MainNode(parseBlock(false))
    }

    private fun parseBlock(inner: Boolean): Block {
        val block = Block()
        if (inner) {
            if (current !is OpenBlock) throw ParserException("Incorrect block format")
            getNext()
        }
        while (true){
            if (inner){
                if (current is EOF){
                    throw ParserException("Unexpected end of file")
                }
                if (current is CloseBlock){
                    getNext()
                    return block
                }
            } else if (current is EOF) {
                return block
            }
            block.statements.add(parseStatement())
        }
    }

    private fun parseStatement(): Statement {
        return when (current) {
            is If -> parseIf()
            is While -> parseWhile()
            is Print -> parsePrint()
            is Identifier -> parseAssignment()
            is TypeIdentifier -> parseDeclOrInit()
            else -> throw ParserException("Incorrect statement")
        }
    }

    private fun parseCondition(): Expression {
        val condition = parseExpression()
        if (condition.returnType != TypeBoolean) throw ParserException("Condition type is not bool")
        return condition
    }

    private fun parseIf(): IfNode {
        getNext()
        val condition = parseCondition()
        if (current !is OpenBlock) throw ParserException("Incorrect block syntax")
        SymbolTable.push()
        val thenBlock = parseBlock(true)
        SymbolTable.pop()
        val elseBlock = if (current is Else) {
            getNext()
            if (current !is OpenBlock) throw ParserException("Incorrect block syntax")
            SymbolTable.push()
            val out = parseBlock(true)
            SymbolTable.pop()
            out
        } else null
        return IfNode(condition, thenBlock, elseBlock)
    }

    private fun parseWhile(): WhileNode {
        getNext()
        val condition = parseCondition()
        if (current !is OpenBlock) throw ParserException("Incorrect block syntax")
        SymbolTable.push()
        val body = parseBlock(true)
        SymbolTable.pop()
        return WhileNode(condition, body)
    }

    private fun parsePrint(): PrintNode {
        getNext()
        val out = PrintNode(parseExpression())
        if (current !is EndLine) throw ParserException("Incorrect statement format")
        else {
            getNext()
            return out
        }
    }

    private fun parseAssignment(): AssignmentNode {
        val symbolN = SymbolTable.get((current as Identifier).name)
        val symbol = (symbolN ?: throw ParserException("Undefined identifier"))
                as? VariableInfo ?: throw ParserException("Lvalue is not variable")
        getNext()
        if (current !is Assignment) throw ParserException("Incorrect assignment syntax")
        getNext()
        val out = AssignmentNode(symbol, parseExpression())
        if (current !is EndLine) throw ParserException("Incorrect statement format")
        else {
            getNext()
            return out
        }
    }

    private fun parseDeclOrInit(): Statement {
        val type = when ((current as TypeIdentifier).type) {
            TypeIdentifier.Type.INT -> TypeInt
            TypeIdentifier.Type.STRING -> TypeString
            TypeIdentifier.Type.BOOLEAN -> TypeBoolean
        }
        getNext()
        val newVal = VariableInfo(
            (current as? Identifier)?.name
                ?: throw ParserException("Incorrect declaration syntax"),
            type)
        if (SymbolTable.contains(newVal.name)) throw ParserException("Variable redefinition")
        getNext()
        return when (current) {
            is Assignment -> {
                getNext()
                val expr = parseExpression()
                if (expr.returnType != newVal.type) throw ParserException("Incorrect initializer type")
                if (current !is EndLine) throw ParserException("Incorrect statement format")
                else {
                    getNext()
                    return VarInitialization(newVal, expr)
                }
            }
            is EndLine -> {
                getNext()
                VarDeclaration(newVal)
            }
            else -> throw ParserException("Incorrect declaration syntax")
        }
    }

    private fun parseExpression(): Expression {
        val first = parseLogicOr()
        return if (current is LogicAnd) {
            getNext()
            LogicBinaryOperator(LogicBinaryOperator.OpKind.AND, first, parseExpression())
        } else first
    }

    private fun parseLogicOr(): Expression {
        val first = parseEquality()
        return if (current is LogicOr) {
            getNext()
            LogicBinaryOperator(LogicBinaryOperator.OpKind.OR, first, parseLogicOr())
        } else first
    }

    private fun parseEquality(): Expression {
        val first = parseComparison()
        return if (current is LogicOperation) when ((current as LogicOperation).opcode) {
            LogicOperation.OperationType.EQ -> {
                getNext()
                CompOperator(CompOperator.OpKind.EQ, first, parseEquality())
            }
            LogicOperation.OperationType.NEQ -> {
                getNext()
                CompOperator(CompOperator.OpKind.NEQ, first, parseEquality())
            }
            else -> throw ParserException("Incorrect expression format")
        } else first
    }

    private fun parseComparison(): Expression {
        val first = parseSumSub()
        return if (current is LogicOperation) when ((current as LogicOperation).opcode) {
            LogicOperation.OperationType.MORE -> {
                getNext()
                val second = parseSumSub()
                if (first.returnType != TypeInt || second.returnType != TypeInt) throw ParserException("Incorrect cmp argument")
                CompOperator(CompOperator.OpKind.MORE, first, second)
            }
            LogicOperation.OperationType.LESS -> {
                getNext()
                val second = parseSumSub()
                if (first.returnType != TypeInt || second.returnType != TypeInt) throw ParserException("Incorrect cmp argument")
                CompOperator(CompOperator.OpKind.LESS, first, second)
            }
            LogicOperation.OperationType.MORE_EQ -> {
                getNext()
                val second = parseSumSub()
                if (first.returnType != TypeInt || second.returnType != TypeInt) throw ParserException("Incorrect cmp argument")
                CompOperator(CompOperator.OpKind.MORE_EQ, first, second)
            }
            LogicOperation.OperationType.LESS_EQ -> {
                getNext()
                val second = parseSumSub()
                if (first.returnType != TypeInt || second.returnType != TypeInt) throw ParserException("Incorrect cmp argument")
                CompOperator(CompOperator.OpKind.LESS_EQ, first, second)
            }
            else -> first
        } else first
    }

    private fun parseSumSub(): Expression {
        val first = parseMultDiv()
        return if (current is MathOperation) {
            when ((current as MathOperation).opcode) {
                MathOperation.OperationType.PLUS -> {
                    getNext()
                    MathBinaryOperator(MathBinaryOperator.OpKind.ADD, first, parseSumSub())
                }
                MathOperation.OperationType.MINUS -> {
                    getNext()
                    MathBinaryOperator(MathBinaryOperator.OpKind.SUB, first, parseSumSub())
                }
                else -> throw ParserException("Incorrect expression format")
            }
        } else first
    }

    private fun parseMultDiv(): Expression {
        val first = parseUnary()
        return if (current is MathOperation) {
            when ((current as MathOperation).opcode) {
                MathOperation.OperationType.MULT -> {
                    getNext()
                    MathBinaryOperator(MathBinaryOperator.OpKind.MULT, first, parseMultDiv())
                }
                MathOperation.OperationType.DIV -> {
                    getNext()
                    MathBinaryOperator(MathBinaryOperator.OpKind.DIV, first, parseMultDiv())
                }
                else -> first
            }
        } else first
    }

    private fun parseUnary(): Expression {
        return if (current is LogicNot){
            getNext()
            LogicUnaryOperator(LogicUnaryOperator.OpKind.NEG, parseAtom())
        } else if (current is MathOperation &&
            (current as MathOperation).opcode == MathOperation.OperationType.MINUS){
            getNext()
            MathUnaryOperator(MathUnaryOperator.OpKind.MINUS, parseAtom())
        } else parseAtom()
    }

    private fun parseAtom(): Expression {
        return when (val toCheck = current) {
            is OpenBracer -> {
                getNext()
                val out = parseExpression()
                if (current is CloseBracer) {
                    getNext()
                    out
                } else throw ParserException("Incorrect expression format")
            }
            is Literal<*> -> {
                when (val value = toCheck.value) {
                    is Int -> {
                        getNext()
                        IntLiteral(value)
                    }
                    is String -> {
                        getNext()
                        StringLiteral(value)
                    }
                    else -> throw ParserException("Unknown literal type")
                }
            }
            is Identifier -> {
                getNext()
                val symbol = SymbolTable.get(toCheck.name)
                Variable(symbol as? VariableInfo
                    ?: throw ParserException("Unknown identifier"))
            }
            else -> throw ParserException("Incorrect expression format")
        }
    }
}