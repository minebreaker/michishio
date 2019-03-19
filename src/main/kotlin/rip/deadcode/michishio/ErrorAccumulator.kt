package rip.deadcode.michishio

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

object ErrorAccumulator : BaseErrorListener() {

    data class SyntaxErrorInfo(
        val offendingSymbol: Any,
        val line: Int,
        val charPositionInLine: Int,
        val msg: String,
        val e: RecognitionException
    )

    val errors = mutableListOf<SyntaxErrorInfo>()

    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException
    ) {
        errors += SyntaxErrorInfo(offendingSymbol, line, charPositionInLine, msg, e)
    }
}
