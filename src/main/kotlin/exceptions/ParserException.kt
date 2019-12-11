package exceptions

import java.lang.Exception

class ParserException(val msg: String = "Some error happen", val inner: Throwable? = null): Exception(msg){
}