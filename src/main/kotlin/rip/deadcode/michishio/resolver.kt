import org.objectweb.asm.Type


internal fun toInternalType(fqcn: String): String {
    return fqcn.replace('.', '/')
}

internal fun toTypeDescriptor(fqcn: String): String {
    return Type.getObjectType(toInternalType(fqcn)).descriptor
}
