package rip.deadcode.michishio

import findClass
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import org.objectweb.asm.*
import resolve
import resolveToDescriptor
import resolveToInternalName
import rip.deadcode.michishio.generated.MichishioLexer
import rip.deadcode.michishio.generated.MichishioParser
import rip.deadcode.michishio.generated.MichishioParser.*
import toInternalType
import toMethodDescriptor
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
        val e = errorAccumulator.errors[0]  // FIXME
        throw MichishioException(
            "rip.deadcode.michishio.2",
            (e.offendingSymbol as Token).text,
            "${e.line}:${e.charPositionInLine}",
            e.msg
        )
    }

    return compileFile(source)
}

private fun compileFile(source: FileContext): ByteArray {

    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    val version = source.version()
    val versionMajor = version.NATURAL(0).text.toInt()
    val versionMinor = version.NATURAL(1).text.toInt()
    val versionInt = (versionMinor shl 16) or versionMajor

    val imports = readImportList(source.import_declaration())
    val importNames = imports.map { it.canonicalName }

    val classDec = source.class_declaration()
    val classAccFlag = compileClassAccessFlag(classDec.class_access_flag())
    val className = toInternalType(classDec.java_type_name().text)

    val (superClass, interfaces) = compileInheritance(classDec, importNames)

    writer.visit(versionInt, classAccFlag, className, null, superClass, interfaces)

    classDec.field_declaration().forEach {
        compileField(writer, it, importNames)
    }
    classDec.method_declaration().forEach {
        compileMethod(writer, it, importNames)
    }

    writer.visitEnd()
    return writer.toByteArray()
}

private fun compileClassAccessFlag(nodes: List<Class_access_flagContext>): Int {
    // TODO check if valid flag
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
        .reduce(0) { acc, n -> acc or n }
}

private fun readImportList(importContexts: List<Import_declarationContext>): List<Class<*>> {
    return importContexts
        .map {
            val target = it.java_type_name().text
            findClass(target) ?: throw MichishioException("rip.deadcode.michishio.3", target)
        }
}

private fun compileInheritance(classDec: Class_declarationContext, imports: List<String>): Pair<String, Array<String>> {

    val inheritance = classDec.inheritance()
    val extension = if (inheritance.java_type_name() != null) {
        resolveToInternalName(inheritance.java_type_name().text, imports)
    } else {
        "java/lang/Object"
    }
    val interfaces = inheritance.interfaces()?.java_type_name()
        ?.map { resolveToInternalName(it.text, imports) }
        ?.toTypedArray() ?: arrayOf()
    return extension to interfaces
}

private fun compileField(writer: ClassWriter, field: Field_declarationContext, imports: List<String>) {

    val fieldAccFlag = compileFieldAccessFlag(field.field_access_flag())
    val fieldName = field.java_type_name().text
    val descriptor = compileFieldDescriptor(field.field_type(), imports)

    val value = field.constant_field_notation()?.STRING_LITERAL()?.text?.decodeStringLiteral()  // TODO

    val fv = writer.visitField(fieldAccFlag, fieldName, descriptor, null, value)
    if (field.attribute_notation() != null) {
        field.attribute_notation().attribute().forEach {
            compileFieldAttribute(writer, fv, it)
        }
    }
    fv.visitEnd()
}

private fun compileFieldAccessFlag(nodes: List<Field_access_flagContext>): Int {
    // TODO check if valid flag
    return flagsToInt(nodes.map { it.text })
}

private fun compileFieldDescriptor(fieldType: Field_typeContext, imports: List<String>): String {
    return if (fieldType.STRING_LITERAL() != null) {
        fieldType.STRING_LITERAL().text.decodeStringLiteral()
    } else {
        resolveToDescriptor(fieldType.java_type_name().text, imports)
    }
}

private fun compileFieldAttribute(writer: ClassWriter, fv: FieldVisitor, attribute: AttributeContext) {

    val predefinedAttribute = attribute.predefined_attribute()
    if (predefinedAttribute != null) {
        if (predefinedAttribute.constant_value_attribute() != null) {
            val value = predefinedAttribute.constant_value_attribute().STRING_LITERAL().text.decodeStringLiteral()
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
        compileGeneralAttribute(generalAttribute, fv::visitAttribute, writer)
    }
}

private fun compileMethod(writer: ClassWriter, method: Method_declarationContext, imports: List<String>) {

    val methodAccFlag = compileMethodAccessFlag(method.method_access_flag())
    val methodName = method.java_type_name().text

    val (descriptor, signature) =
        compileMethodDescriptor(method.method_return_type(), method.method_arguments(), imports)
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
    argumentTypes: Method_argumentsContext?,
    imports: List<String>
): Pair<String, String?> {

    val inlineDescriptor = returnType.STRING_LITERAL()
    if (inlineDescriptor == null) {

        val returnTypeClass = if (returnType.java_type_name() == null) {
            Void.TYPE
        } else {
            resolve(returnType.java_type_name().text, imports)
        }
        val argumentClasses = argumentTypes?.method_argument()
            ?.map { resolve(it.java_type_name().text, imports) }
            ?: listOf()

        return toMethodDescriptor(returnTypeClass, argumentClasses) to null

    } else {
        if (argumentTypes != null) {
            throw RuntimeException("Inline descriptor with arguments.")
        }

        return inlineDescriptor.text.decodeStringLiteral() to null
    }
}

private fun compileMethodAttribute(writer: ClassWriter, mv: MethodVisitor, attribute: AttributeContext) {

    val predefinedAttribute = attribute.predefined_attribute()
    if (predefinedAttribute != null) {
        if (predefinedAttribute.code_attribute() != null) {
            val operations = predefinedAttribute.code_attribute().operation()
            // TODO
            mv.visitCode()
            operations.forEach { op ->
                when (op.instruction().text) {
                    "getstatic" -> {
                        mv.visitFieldInsn(
                            Opcodes.GETSTATIC,
                            op.argument(0).STRING_LITERAL().text.decodeStringLiteral(),
                            op.argument(1).STRING_LITERAL().text.decodeStringLiteral(),
                            op.argument(2).STRING_LITERAL().text.decodeStringLiteral()
                        )
                    }
                    "ldc" -> {
                        mv.visitLdcInsn(op.argument(0).STRING_LITERAL().text.decodeStringLiteral())
                    }
                    "invokevirtual" -> {
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            op.argument(0).STRING_LITERAL().text.decodeStringLiteral(),
                            op.argument(1).STRING_LITERAL().text.decodeStringLiteral(),
                            op.argument(2).STRING_LITERAL().text.decodeStringLiteral(),
                            false
                        )
                    }
                    "return" -> {
                        mv.visitInsn(Opcodes.RETURN)
                    }
                    else -> {
                        throw IllegalStateException("Unknown instruction: " + op.instruction().text)
                    }
                }


            }

            mv.visitMaxs(0, 0)  // regardless of the value, the stack and the local are calculated.
            mv.visitEnd()
        }
    }
    val generalAttribute = attribute.general_attribute()
    if (generalAttribute != null) {
        compileGeneralAttribute(generalAttribute, mv::visitAttribute, writer)
    }
}

fun compileGeneralAttribute(
    generalAttribute: General_attributeContext,
    visit: (Attribute) -> Unit,
    writer: ClassWriter
) {

    val attributeName = generalAttribute.STRING_LITERAL().text.decodeStringLiteral()
    val value = generalAttribute.attribute_value()[0].STRING_LITERAL().text.decodeStringLiteral()  // TODO
    visit(object : Attribute(attributeName) {
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
