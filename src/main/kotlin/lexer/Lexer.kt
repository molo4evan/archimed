package lexer

import exceptions.LexerException
import java.io.Reader
import java.lang.NumberFormatException
import java.lang.StringBuilder

class Lexer(private val reader: Reader) {
    private var current: Int = reader.read()

    private fun readNext(){
        current = reader.read()
    }

    private fun parseStringLiteral(): Literal<String> {
        readNext()
        val string = StringBuilder().append(current.toChar())
        readNext()
        while (current.toChar() != '"'){
            if (current == -1) throw LexerException(msg = "EOF")
            else string.append(current.toChar())
            readNext()
        }
        readNext()
        return Literal(string.toString())
    }

    private fun parseIntLiteral(): Literal<Int> {
        val number = StringBuilder().append(current.toChar())
        readNext()
        while (current.toChar().isDigit()){
            number.append(current.toChar())
            readNext()
        }
        return try {
            Literal(number.toString().toInt())
        } catch (ex: NumberFormatException){
            throw LexerException(ex)
        }
    }

    private fun tryParseKeyword(): Lexeme {
        val string = StringBuilder().append(current.toChar())
        readNext()
        var char = current.toChar()
        while (char.isLetterOrDigit()){
            string.append(char)
            readNext()
            char = current.toChar()
        }
        return when(val value = string.toString()) {
            "print"-> Print
            "if" -> If
            "else" -> Else
            "while" -> While
            "int" -> TypeIdentifier(TypeIdentifier.Type.INT)
            "string" -> TypeIdentifier(TypeIdentifier.Type.STRING)
            "bool" -> TypeIdentifier(TypeIdentifier.Type.BOOLEAN)
            else -> Identifier(value)
        }
    }

    fun getLexeme(): Lexeme {
        if (current == -1) return EOF
        while (current.toChar().isWhitespace()){
            readNext()
            if (current == -1) return EOF
        }
        val symbol = current.toChar()

        return when {
            symbol == ';' -> {
                readNext()
                EndLine
            }
            symbol == '(' -> {
                readNext()
                OpenBracer
            }
            symbol == ')' -> {
                readNext()
                CloseBracer
            }
            symbol == '{' -> {
                readNext()
                OpenBlock
            }
            symbol == '}' -> {
                readNext()
                CloseBlock
            }
            symbol == '+' -> {
                readNext()
                MathOperation(MathOperation.OperationType.PLUS)
            }
            symbol == '-' -> {
                readNext()
                MathOperation(MathOperation.OperationType.MINUS)
            }
            symbol == '*' -> {
                readNext()
                MathOperation(MathOperation.OperationType.MULT)
            }
            symbol == '/' -> {
                readNext()
                MathOperation(MathOperation.OperationType.DIV)
            }
            symbol == '<' -> {
                readNext()
                if (current.toChar() == '='){
                    readNext()
                    LogicOperation(LogicOperation.OperationType.LESS_EQ)
                } else LogicOperation(LogicOperation.OperationType.LESS)
            }
            symbol == '>' -> {
                readNext()
                if (current.toChar() == '='){
                    readNext()
                    LogicOperation(LogicOperation.OperationType.MORE_EQ)
                } else LogicOperation(LogicOperation.OperationType.MORE)
            }
            symbol == '=' -> {
                readNext()
                if (current.toChar() != '='){
                    Assignment
                } else {
                    readNext()
                    LogicOperation(LogicOperation.OperationType.EQ)
                }
            }
            symbol == '!' -> {
                readNext()
                if (current.toChar() == '='){
                    readNext()
                    LogicOperation(LogicOperation.OperationType.NEQ)
                } else LogicNot
            }
            symbol == '&' -> {
                readNext()
                if (symbol == '&'){
                    readNext()
                    LogicAnd
                } else throw LexerException()
            }
            symbol == '|' -> {
                readNext()
                if (symbol == '|'){
                    readNext()
                    LogicOr
                } else throw LexerException()
            }
            symbol == '"' -> {
                parseStringLiteral()
            }
            symbol.isDigit() -> {
                parseIntLiteral()
            }
            else -> tryParseKeyword()
        }
    }
}