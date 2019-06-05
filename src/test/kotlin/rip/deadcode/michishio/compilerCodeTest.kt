package rip.deadcode.michishio

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test


class CompilerCodeKtTest {

    @Test
    fun test() {

        val source = """
            major 52;
            minor 0;

            public final super class test.TestClass {
                public static "()Ljava/lang/String;" mLdc() {
                    Code {
                        ldc "str";
                        areturn;
                    }
                }
                public static "()I" mIconstM1() {
                    Code {
                        iconst_m1;
                        ireturn;
                    }
                }
                public static "()I" mIconst0() {
                    Code {
                        iconst_0;
                        ireturn;
                    }
                }
                public static "()I" mIconst1() {
                    Code {
                        iconst_1;
                        ireturn;
                    }
                }
                public static "()I" mIconst2() {
                    Code {
                        iconst_2;
                        ireturn;
                    }
                }
                public static "()I" mIconst3() {
                    Code {
                        iconst_3;
                        ireturn;
                    }
                }
                public static "()I" mIconst4() {
                    Code {
                        iconst_4;
                        ireturn;
                    }
                }
                public static "()I" mIconst5() {
                    Code {
                        iconst_5;
                        ireturn;
                    }
                }
            }
        """.trimIndent()

        val cls = load(source, "test.TestClass")

        val mLdc = cls.getDeclaredMethod("mLdc")
        assertThat(mLdc(null)).isEqualTo("str")

        val mIconstM1 = cls.getDeclaredMethod("mIconstM1")
        assertThat(mIconstM1(null)).isEqualTo(-1)

        val mIconst0 = cls.getDeclaredMethod("mIconst0")
        assertThat(mIconst0(null)).isEqualTo(0)

        val mIconst1 = cls.getDeclaredMethod("mIconst1")
        assertThat(mIconst1(null)).isEqualTo(1)

        val mIconst2 = cls.getDeclaredMethod("mIconst2")
        assertThat(mIconst2(null)).isEqualTo(2)

        val mIconst3 = cls.getDeclaredMethod("mIconst3")
        assertThat(mIconst3(null)).isEqualTo(3)

        val mIconst4 = cls.getDeclaredMethod("mIconst4")
        assertThat(mIconst4(null)).isEqualTo(4)

        val mIconst5 = cls.getDeclaredMethod("mIconst5")
        assertThat(mIconst5(null)).isEqualTo(5)
    }
}
