package io.lilmonk.utils

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class FileReader {
  def read(fileName: String): List[String] = {
    val queries = ArrayBuffer[String]()
    val bufferedSource = Source.fromFile(fileName)
    val currentProcessingQuery = ArrayBuffer[String]()
    for (line <- bufferedSource.getLines) {
      val trimmedLine = line.trim
      if (trimmedLine.nonEmpty)
        currentProcessingQuery.append(line)
      if (trimmedLine.endsWith(";")) {
        queries += currentProcessingQuery.mkString("\n")
        currentProcessingQuery.clear()
      }
    }
    bufferedSource.close
    queries.toList
  }
}

object FileReader {
  def apply(): FileReader = new FileReader()
}
