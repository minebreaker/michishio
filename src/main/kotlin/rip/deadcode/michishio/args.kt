package rip.deadcode.michishio

import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.Options


data class Config(
    val input: String,
    val output: String
)

private val options = Options()
    .addOption("o", "output", true, "Output file name")

fun parseArgs(args: Array<String>): Config {

    val command = Toolbox[CommandLineParser::class].parse(options, args)

    // TODO checks

    return Config(command.args[0], command.getOptionValue("output")!!)
}
