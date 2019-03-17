package rip.deadcode.michishio

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ArgsKtTest {

    @Test
    fun testParseArgs() {

        var args = arrayOf("-o", "foo", "bar")
        var result = parseArgs(args)
        assertThat(result.input).isEqualTo("bar")
        assertThat(result.output).isEqualTo("foo")

        args = arrayOf("bar", "-o", "foo")
        result = parseArgs(args)
        assertThat(result.input).isEqualTo("bar")
        assertThat(result.output).isEqualTo("foo")
    }
}
