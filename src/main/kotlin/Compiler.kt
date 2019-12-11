import lexer.Lexer
import parser.Parser
import visitor.ByteCodeVisitor
import java.io.File
import java.io.FileReader
import java.io.FileOutputStream

fun main(args: Array<String>) {
    if (args.isEmpty() || args.size > 2) throw Exception("Incorrect argument number")
    val sourceFile = args[0]
    val destFilename = if (args.size == 2) args[1] else null
    val parser = Parser(Lexer(FileReader(sourceFile)))
    val ast = parser.parseMain()
    val visitor = ByteCodeVisitor()
    if (destFilename != null) visitor.setClassName(destFilename)
    visitor.visit(ast)
    val bytecode = visitor.getResult()
    val file = File("${destFilename ?: "Main"}.class")
    if (file.exists()) file.delete()
    file.createNewFile()
    val out = FileOutputStream(file)
    out.write(bytecode)
    out.flush()
    out.close()
}