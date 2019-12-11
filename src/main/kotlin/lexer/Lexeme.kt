package lexer

sealed class Lexeme

object EOF: Lexeme()
object EndLine: Lexeme()
data class Identifier(val name: String): Lexeme()
data class Literal<T>(val value: T): Lexeme()
object Print: Lexeme()
object If: Lexeme()
object Else: Lexeme()
object While: Lexeme()
object Assignment: Lexeme()
object OpenBracer: Lexeme()
object CloseBracer: Lexeme()
object OpenBlock: Lexeme()
object CloseBlock: Lexeme()
data class TypeIdentifier(val type: Type): Lexeme(){
    enum class Type {
        INT,
        STRING,
        BOOLEAN
    }
}
data class MathOperation(val opcode: OperationType): Lexeme(){
    enum class OperationType{
        PLUS,
        MINUS,
        MULT,
        DIV
    }
}
data class LogicOperation(val opcode: OperationType): Lexeme(){
    enum class OperationType{
        MORE,
        LESS,
        EQ,
        NEQ,
        MORE_EQ,
        LESS_EQ
    }
}
object LogicAnd: Lexeme()
object LogicOr: Lexeme()
object LogicNot: Lexeme()