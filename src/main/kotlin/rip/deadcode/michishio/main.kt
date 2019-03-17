package rip.deadcode.michishio

import rip.deadcode.michishio.debug.performance
import java.nio.file.FileSystem
import java.nio.file.Files


fun main(args: Array<String>) {

    val config = parseArgs(args)

    val fs = Toolbox[FileSystem::class]

    val sourcePath = fs.getPath(config.input)
    val out = Files.newInputStream(sourcePath).use {
        performance {
            compile(it)
        }
    }

    val destination = fs.getPath(config.output)
    Files.write(destination, out)
}
