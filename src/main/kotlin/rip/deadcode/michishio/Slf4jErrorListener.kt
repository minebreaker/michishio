package rip.deadcode.michishio

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.slf4j.LoggerFactory

object Slf4jErrorListener : BaseErrorListener() {

    private val logger = LoggerFactory.getLogger(Slf4jErrorListener::class.java)

    override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?) {
        logger.error("line {}:{} {}", line, charPositionInLine, msg)
    }
}
