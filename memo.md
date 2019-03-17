# Michishio

Programming language for writing Java byte code


TODO

* signature syntax
* import resolution
* constants
* attributes
    * code
* exception syntax
* try-catch syntax
* fluent error message
* inner class

```
major 52;
minor 0;

// Imported classes can be used in descriptor declarations.
// Classes in the `java.lang` package is imported by default.
import java.io.PrintStream;


// Constant pool declarations can be omitted.
//
// constant {
//     Utf8 "Explicit declaration"
//
//     Utf8 "sample/SampleClass" as alias
//     Class $alias
// }

// Class name
public final super sample.HelloClass {  // extends Object implements Interface
// If `extends` is omitted, `java.lang.Object` is used.

// Fields
    
    public static final String MESSAGE;
//
//     Attribute
//     public static final String MESSAGE {
//         ConstantValue: $message;
//     }
//
//     Inline descriptor
//     public static final "Ljava/lang/String;" MESSAGE
//
//     Value notation
//     public static final String MESSAGE = "hello, world";
//
//     Constant notation
//     public static final String MESSAGE = message;


    // Methods
    
    public static void main(String)

    // Inline descriptor
    // public static "([Ljava/lang/String;)V" main() {
    
        // Code attribute notation
        Code {
            getstatic System.out;
            // getstatic System.out at 0  // Line number notation
            getstatic MESSAGE
            invokevirtual (PrintStream.println(String)) -> void
        }
        // Hex literal. Attribute length is calculated.
        "OtherAttribute" = 0x 01 23 AB CD
    }

    // Class attributes
    SourceFile = $sourcefile

}
```
