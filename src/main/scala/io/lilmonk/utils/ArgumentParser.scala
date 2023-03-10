package io.lilmonk.utils

import io.lilmonk.option.AppOption
import picocli.CommandLine

object ArgumentParser {
  /**
   * Parse commandline arguments.
   *
   * @param args Commandline arguments.
   * @return [[AppOption]]
   */
  def getOptions(args: Array[String]): AppOption = {
    val options = new AppOption
    val commandLine = new CommandLine(options)
    commandLine.setUnmatchedArgumentsAllowed(true)
    commandLine.parseArgs(args: _*)
    options
  }
}
