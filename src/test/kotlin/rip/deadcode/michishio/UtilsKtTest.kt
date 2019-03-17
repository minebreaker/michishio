package rip.deadcode.michishio

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class UtilsKtTest {

    @Test
    fun testUnquote() {

        val result = "\"foo\"".unquote()
        assertThat(result).isEqualTo("foo")
    }
}