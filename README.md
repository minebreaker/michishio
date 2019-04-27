# Michishio

Programming language for writing Java byte code


```
major 52;
minor 0;

public final super class HelloClass {
    public static "([Ljava/lang/String;)V" main() {
        Code {
            getstatic "java/lang/System" "out" "Ljava/io/PrintStream;";
            ldc "hello, world"
            invokevirtual "java/io/PrintStream" "println" "(Ljava/lang/String;)V";
            return;
        }
    }
}
```
