package rip.deadcode.michishio

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import rip.deadcode.izvestia.Core.expect
import java.io.ByteArrayInputStream
import java.lang.reflect.Modifier
import java.nio.ByteBuffer
import java.util.*

internal class CompilerKtTest {

    @Test
    fun testImports() {

        val source = """
            major 52;
            minor 0;

            import java.util.Date;
            import java.util.List;

            public final super class test.TestClass
                extends Date
                implements List, Runnable, java.lang.CharSequence {}
        """.trimIndent()

        val classfile = compile(ByteArrayInputStream(source.toByteArray()))
        val cls = ByteClassLoader("test.TestClass", classfile).loadClass("")

        assertThat(cls.superclass).isEqualTo(Date::class.java)
        assertThat(cls.interfaces.asList())
            .containsExactly(List::class.java, Runnable::class.java, CharSequence::class.java)
    }

    @Test
    fun testClass() {

        val source = """
            major 45;
            minor 3;

            // Comment
            /*
             * Multi Line Comment
             */

            public final super class test.TestClass
                extends java.util.Date
                implements java.lang.Runnable, java.lang.CharSequence {}
        """.trimIndent()

        val classfile = compile(ByteArrayInputStream(source.toByteArray()))
        val cls = ByteClassLoader("test.TestClass", classfile).loadClass("")

        assertThat(cls.canonicalName).isEqualTo("test.TestClass")
        assertThat(Modifier.isPublic(cls.modifiers)).isTrue()
        assertThat(Modifier.isFinal(cls.modifiers)).isTrue()

        assertThat(cls.superclass).isEqualTo(Date::class.java)
        assertThat(cls.interfaces.asList()).containsExactly(Runnable::class.java, CharSequence::class.java)

        val bb = ByteBuffer.wrap(classfile)
        assertThat(bb.int).isEqualTo(-889275714)
        assertThat(bb.short).isEqualTo(3)
        assertThat(bb.short).isEqualTo(45)
    }

    @Test
    fun testField() {

        val source = """
            major 52;
            minor 0;

            public final super class test.TestClass {
                public java.lang.String field1;
                public static "Ljava/lang/String;" field2;
                public static java.lang.String field3 = "test";
                public static java.lang.String field4 {
                    ConstantValue = "test";
                }
            }
        """.trimIndent()

        val cls = load(source, "test.TestClass")
        assertThat(cls.declaredFields.size).isEqualTo(4)

        val f1 = cls.getDeclaredField("field1")
        assertThat(f1.type).isEqualTo(String::class.java)
        assertThat(Modifier.isPublic(f1.modifiers)).isTrue()

        val f2 = cls.getDeclaredField("field2")
        assertThat(f2.type).isEqualTo(String::class.java)
        assertThat(Modifier.isPublic(f2.modifiers)).isTrue()

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

    @Test
    fun testFieldImport() {

        val source = """
            major 52;
            minor 0;

            import java.util.Date;

            public final super class test.TestClass {
                String field1;
                Date field2;
            }
        """.trimIndent()

        val cls = load(source, "test.TestClass")
        assertThat(cls.declaredFields.size).isEqualTo(2)

        val f1 = cls.getDeclaredField("field1")
        assertThat(f1.type).isEqualTo(String::class.java)

        val f2 = cls.getDeclaredField("field2")
        assertThat(f2.type).isEqualTo(Date::class.java)
    }

    @Test
    fun testError() {
        expect {
            val source = "unexpected token"
            compile(ByteArrayInputStream(source.toByteArray()))

        }.throwsException {
            assertThat(it).isInstanceOf(MichishioException::class.java)
        }
    }

    private fun load(code: String, name: String): Class<*> {
        val classfile = compile(ByteArrayInputStream(code.toByteArray()))
        return ByteClassLoader(name, classfile).loadClass("")
    }

    class ByteClassLoader(private val className: String, private val classfile: ByteArray) : ClassLoader() {
        override fun findClass(name: String?): Class<*> {
            return defineClass(this.className, classfile, 0, classfile.size)
        }
    }
}
