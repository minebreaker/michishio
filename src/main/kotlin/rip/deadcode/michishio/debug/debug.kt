package rip.deadcode.michishio.debug

import com.google.common.base.Stopwatch
import org.slf4j.LoggerFactory
import rip.deadcode.michishio.getMessage


private val ea = Class.forName("rip.deadcode.michishio.debug.DebugKt").desiredAssertionStatus()

fun assert(assertion: () -> Boolean) {
    if (ea) kotlin.assert(assertion())
}

fun <T> performance(block: ()->T): T {
    return if (ea) {

        val stopwatch = Stopwatch.createStarted()
        val result = block()
        val duration = stopwatch.stop().elapsed()
        LoggerFactory.getLogger("rip.deadcode.michishio.debug")
            .info(getMessage("rip.deadcode.michishio.1"), duration.nano)

        result

    } else {
        block()
    }
}
