package rip.deadcode.michishio

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import org.objectweb.asm.*
import rip.deadcode.michishio.generated.MichishioLexer
import rip.deadcode.michishio.generated.MichishioParser
import rip.deadcode.michishio.generated.MichishioParser.*
import java.io.InputStream


fun compile(input: InputStream): ByteArray {

    val stream = CharStreams.fromStream(input)
    val errorAccumulator = ErrorAccumulator()
    val lexer = MichishioLexer(stream).apply {
        removeErrorListeners()
        addErrorListener(errorAccumulator)
    }
    val tokens = BufferedTokenStream(lexer)
    val parser = MichishioParser(tokens).apply {
        removeErrorListeners()
        addErrorListener(errorAccumulator)
    }
    val source = parser.file()

    if (errorAccumulator.errors.isNotEmpty()) {
        val e = errorAccumulator.errors[0]
        throw MichishioException(
            getMessage("rip.deadcode.michishio.2").format(
                (e.offendingSymbol as Token).text, "${e.line}:${e.charPositionInLine}", e.msg
            )
        )
    }

    return compileFile(source)
}

private fun compileFile(source: MichishioParser.FileContext): ByteArray {

    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    val version = source.version()
    val versionMajor = version.NATURAL(0).text.toInt()
    val versionMinor = version.NATURAL(1).text.toInt()
    val versionInt = (versionMinor shl 16) or versionMajor

    val classDec = source.class_declaration()
    val classAccFlag = compileClassAccessFlag(classDec.class_access_flag())
    val className = classDec.java_type_name().text.replace('.', '/')

    val (superClass, interfaces) = compileInheritance(classDec)

    writer.visit(versionInt, classAccFlag, className, null, superClass, interfaces)

    classDec.field_declaration().forEach {
        compileField(writer, it)
    }
    classDec.method_declaration().forEach {
        compileMethod(writer, it)
    }

    writer.visitEnd()
    return writer.toByteArray()
}

private fun compileClassAccessFlag(nodes: List<Class_access_flagContext>): Int {
    return flagsToInt(nodes.map { it.text })
}

private fun flagsToInt(keywords: List<String>): Int {
    return keywords.stream()
        .mapToInt {
            when (it) {
                "public" -> Opcodes.ACC_PUBLIC
                "final" -> Opcodes.ACC_FINAL
                "super" -> Opcodes.ACC_SUPER
                "interface" -> Opcodes.ACC_INTERFACE
                "abstract" -> Opcodes.ACC_ABSTRACT
                "synthetic" -> Opcodes.ACC_SYNTHETIC
                "annotation" -> Opcodes.ACC_ANNOTATION
                "enum" -> Opcodes.ACC_ENUM
                "module" -> Opcodes.ACC_MODULE
                "private" -> Opcodes.ACC_PRIVATE
                "protected" -> Opcodes.ACC_PROTECTED
                "static" -> Opcodes.ACC_STATIC
                "volatile" -> Opcodes.ACC_VOLATILE
                "transient" -> Opcodes.ACC_TRANSIENT
                "synchronized" -> Opcodes.ACC_SYNCHRONIZED
                "bridge" -> Opcodes.ACC_BRIDGE
                "varargs" -> Opcodes.ACC_VARARGS
                "native" -> Opcodes.ACC_NATIVE
                "strict" -> Opcodes.ACC_STRICT
                else -> {
                    throw RuntimeException()  // TODO
                }
            }
        }
        .reduce { acc, n -> acc or n }
        .asInt
}

private fun compileInheritance(classDec: Class_declarationContext): Pair<String, Array<String>> {
    val inheritance = classDec.inheritance()
    val extension = inheritance.java_type_name()?.text?.replace('.', '/') ?: "java/lang/Object"
    val interfaces = inheritance.interfaces()?.java_type_name()
        ?.map { it.text.replace('.', '/') }
        ?.toTypedArray() ?: arrayOf()
    return extension to interfaces
}

private fun compileField(writer: ClassWriter, field: Field_declarationContext) {

    val fieldAccFlag = compileFieldAccessFlag(field.field_access_flag())
    val fieldName = field.java_type_name().text
    val descriptor = "L" + field.field_type().java_type_name().text.replace('.', '/') + ";"  // TODO

    val value = field.constant_field_notation()?.STRING_LITERAL()?.text?.unquote()  // TODO

    val fv = writer.visitField(fieldAccFlag, fieldName, descriptor, null, value)
    if (field.attribute_notation() != null) {
        field.attribute_notation().attribute().forEach {
            compileFieldAttribute(writer, fv, it)
        }
    }
    fv.visitEnd()
}

private fun compileFieldAccessFlag(nodes: List<Field_access_flagContext>): Int {
    return flagsToInt(nodes.map { it.text })
}

private fun compileFieldAttribute(writer: ClassWriter, fv: FieldVisitor, attribute: AttributeContext) {

    val predefinedAttribute = attribute.predefined_attribute()
    if (predefinedAttribute != null) {
        if (predefinedAttribute.constant_value_attribute() != null) {
            val value = predefinedAttribute.constant_value_attribute().STRING_LITERAL().text.unquote()
            fv.visitAttribute(object : Attribute("ConstantValue") {
                override fun write(
                    classWriter: ClassWriter?,
                    code: ByteArray?,
                    codeLength: Int,
                    maxStack: Int,
                    maxLocals: Int
                ): ByteVector {
                    val i = writer.newConst(value)
                    return ByteVector().putShort(i)
                }
            })
        }
    }
    val generalAttribute = attribute.general_attribute()
    if (generalAttribute != null) {
        val attributeName = generalAttribute.STRING_LITERAL().text.unquote()
        val value = generalAttribute.attribute_value()[0].STRING_LITERAL().text.unquote()  // TODO
        fv.visitAttribute(object : Attribute(attributeName) {
            override fun write(
                classWriter: ClassWriter?,
                code: ByteArray?,
                codeLength: Int,
                maxStack: Int,
                maxLocals: Int
            ): ByteVector {
                val i = writer.newConst(value)
                return ByteVector().putShort(i)
            }
        })
    }
}

private fun compileMethod(writer: ClassWriter, method: Method_declarationContext) {

    val methodAccFlag = compileMethodAccessFlag(method.method_access_flag())
    val methodName = method.java_type_name().text

    val (descriptor, signature) = compileMethodDescriptor(method.method_return_type(), method.method_arguments())
    val mv = writer.visitMethod(methodAccFlag, methodName, descriptor, signature, arrayOf())

    if (method.attribute_notation() != null) {
        method.attribute_notation().attribute().forEach {
            compileMethodAttribute(writer, mv, it)
        }
    }
}

private fun compileMethodAccessFlag(nodes: List<Method_access_flagContext>): Int {
    return flagsToInt(nodes.map { it.text })
}

private fun compileMethodDescriptor(
    returnType: Method_return_typeContext,
    argumentTypes: Method_argumentsContext?
): Pair<String, String?> {

    val inlineDescriptor = returnType.STRING_LITERAL()
    if (inlineDescriptor == null) {
        TODO()

    } else {
        if (argumentTypes != null) {
            throw RuntimeException("Inline descriptor with arguments.")
        }

        return inlineDescriptor.text.unquote() to null
    }
}

private fun compileMethodAttribute(writer: ClassWriter, mv: MethodVisitor, attribute: AttributeContext) {

    val predefinedAttribute = attribute.predefined_attribute()
    if (predefinedAttribute != null) {
        if (predefinedAttribute.code_attribute() != null) {
            TODO()
//            mv.visitCode()
//            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
//            mv.visitLdcInsn("hello, world")
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
//            mv.visitInsn(Opcodes.RETURN)
//            mv.visitMaxs(0, 0)  // regardless of the value, the stack and the local are calculated.
//            mv.visitEnd()
        }
    }
    val generalAttribute = attribute.general_attribute()
    if (generalAttribute != null) {
        val attributeName = generalAttribute.STRING_LITERAL().text.unquote()
        val value = generalAttribute.attribute_value()[0].STRING_LITERAL().text.unquote()  // TODO
        mv.visitAttribute(object : Attribute(attributeName) {
            override fun write(
                classWriter: ClassWriter?,
                code: ByteArray?,
                codeLength: Int,
                maxStack: Int,
                maxLocals: Int
            ): ByteVector {
                val i = writer.newConst(value)
                return ByteVector().putShort(i)
            }
        })
    }
}
