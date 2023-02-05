package io.lilmonk.option

import picocli.CommandLine

class AppOption {
  @CommandLine.Option(names = Array("-s"), description = Array("source queries file"), required = true)
  var sourceQueriesFile: String = _

  @CommandLine.Option(names = Array("-o"), description = Array("sink queries file"), required = true)
  var sinkQueriesFile: String = _

  @CommandLine.Option(names = Array("-q"), description = Array("queries file"), required = true)
  var queriesFile: String = _
}