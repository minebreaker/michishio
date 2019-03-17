package rip.deadcode.michishio

import com.google.common.collect.MutableClassToInstanceMap
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.util.*
import kotlin.reflect.KClass

object Toolbox {

    private val di = MutableClassToInstanceMap.create<Any>()

    init {
        set(CommandLineParser::class, DefaultParser())
        set(ResourceBundle::class, ResourceBundle.getBundle("message"))
        set(FileSystem::class, FileSystems.getDefault())
    }

    operator fun <T : Any> get(cls: KClass<T>): T {
        return di.getInstance(cls.java)!!
    }

    operator fun <T : Any> set(cls: KClass<T>, instance: T) {
        di.putInstance(cls.java, instance)
    }
}
