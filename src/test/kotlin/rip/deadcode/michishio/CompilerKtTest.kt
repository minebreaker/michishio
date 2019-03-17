package rip.deadcode.michishio

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.lang.reflect.Modifier

internal class CompilerKtTest {

    @Test
    fun testClass() {

        val source = """
            major 50;
            minor 0;

            public final super class test.TestClass {
            }
        """.trimIndent()

        val cls = load(source, "test.TestClass")
        assertThat(cls.canonicalName).isEqualTo("test.TestClass")
        assertThat(Modifier.isPublic(cls.modifiers)).isTrue()
        assertThat(Modifier.isFinal(cls.modifiers)).isTrue()
    }

    @Test
    fun testField() {

        val source = """
            major 52;
            minor 0;

            public final super class test.TestClass {
                public java.lang.String field1;
                public static java.lang.String field3 = "test";
                public static java.lang.String field4 {
                    ConstantValue = "test";
                }
            }
        """.trimIndent()
        /*
                public static final "Ljava/lang/String;" field2;


         */

        val cls = load(source, "test.TestClass")
        assertThat(cls.declaredFields.size).isEqualTo(3)

        val f1 = cls.getDeclaredField("field1")
        assertThat(f1.type).isEqualTo(String::class.java)
        assertThat(Modifier.isPublic(f1.modifiers)).isTrue()

        val f3 = cls.getDeclaredField("field3")
        assertThat(f3.type).isEqualTo(String::class.java)
        assertThat(Modifier.isPublic(f3.modifiers)).isTrue()
        assertThat(Modifier.isStatic(f3.modifiers)).isTrue()
        assertThat(f3[null]).isEqualTo("test")

        val f4 = cls.getDeclaredField("field4")
        assertThat(f4.type).isEqualTo(String::class.java)
        assertThat(Modifier.isPublic(f4.modifiers)).isTrue()
        assertThat(Modifier.isStatic(f4.modifiers)).isTrue()
        assertThat(f4[null]).isEqualTo("test")
    }

    private fun load(code: String, name: String): Class<*> {
        val classfile = compile(ByteArrayInputStream(code.toByteArray()))
        return ByteClassLoader(name, classfile).loadClass("")
    }

    class ByteClassLoader(private val name: String, private val classfile: ByteArray) : ClassLoader() {
        override fun findClass(name: String?): Class<*> {
            return defineClass(this.name, classfile, 0, classfile.size)
        }
    }
}
