package rip.deadcode.michishio

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class UtilsKtTest {

    @Test
    fun testUnquote() {

        val result = """"foo\b\t\n\f\r\"\'\\"""".decodeStringLiteral()
        assertThat(result).isEqualTo("foo\b\t\n\u000c\r\"\'\\")
    }
}