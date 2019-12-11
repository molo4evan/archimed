package exceptions

import java.lang.Exception

class LexerException(val inner: Throwable? = null, val msg: String = "Some error happen"): Exception(msg){
}