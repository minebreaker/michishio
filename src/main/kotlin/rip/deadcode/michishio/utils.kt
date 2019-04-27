package rip.deadcode.michishio

import rip.deadcode.michishio.debug.assert
import java.util.*


fun String.decodeStringLiteral(): String {

    assert { this.length > 2 }
    assert { this.startsWith('"') }
    assert { this.endsWith('"') }

    // TODO unicode escape
    return this.substring(1, this.length - 1)
        .replace("\\b", "\b")
        .replace("\\t", "\t")
        .replace("\\n", "\n")
        .replace("\\f", "\u000c")
        .replace("\\r", "\r")
        .replace("\\\"", "\"")
        .replace("\\\'", "'")
        .replace("\\\\", "\\")
}

fun getMessage(key: String): String {
    return Toolbox[ResourceBundle::class].getString(key)
}

fun getMessage(key: String, vararg args: Any): String {
    return Toolbox[ResourceBundle::class].getString(key).format(*args)
}
