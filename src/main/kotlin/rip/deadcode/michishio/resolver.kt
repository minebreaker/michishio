import org.objectweb.asm.Type
import rip.deadcode.michishio.MichishioException
import rip.deadcode.michishio.Toolbox


fun resolveToInternalName(typeName: String, imports: List<String>): String {
    val cls = resolve(typeName, imports) ?: throw MichishioException("rip.deadcode.michishio.3", typeName)
    return toInternalType(cls)
}

fun resolveToDescriptor(typeName: String, imports: List<String>): String {
    val cls = resolve(typeName, imports) ?: throw MichishioException("rip.deadcode.michishio.3", typeName)
    return toDescriptor(cls)
}

fun resolve(typeName: String, imports: List<String>): Class<*>? {
    return findClass(typeName)
        ?: imports
            .filter { it.endsWith(".${typeName}") }
            .mapNotNull { findClass(it) }
            .firstOrNull()
        ?: findClass("java.lang.${typeName}")  // Default import
}

fun findClass(fqcn: String): Class<*>? {
    return try {
        // TODO classpath
        // TODO use independent class loader
        // This import resolution uses compiler's own class path, which can be problematic
        Toolbox[ClassLoader::class].loadClass(fqcn)
    } catch (e: ClassNotFoundException) {
        null
    }
}

fun toInternalType(fqcn: String): String {
    return fqcn.replace('.', '/')
}

fun toInternalType(cls: Class<*>): String {
    return Type.getInternalName(cls)
}

fun toDescriptor(cls: Class<*>): String {
    return Type.getDescriptor(cls)
}
