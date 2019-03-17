package rip.deadcode.michishio

import rip.deadcode.michishio.debug.assert
import java.util.*


fun String.unquote(): String {

    assert { this.length > 2 }
    assert { this.startsWith('"') }
    assert { this.endsWith('"') }

    return this.substring(1, this.length - 1)
}

fun getMessage(key: String): String {
    return Toolbox[ResourceBundle::class].getString(key)
}
