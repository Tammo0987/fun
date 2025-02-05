package com.github.tammo.frontend

import com.github.tammo.core.{Phase, SourceFile, Validated}
import com.github.tammo.diagnostics.CompilerError

import java.nio.file.{Files, Paths}

object InputPhase extends Phase[String, SourceFile] {

  override def run(
      input: String
  ): Validated[List[CompilerError], SourceFile] = {
    val path = Paths.get(input)
    val sourceId = path.toAbsolutePath.toString
    val content = Files.readString(path)
    Validated.valid(SourceFile(sourceId, content))
  }

}
