package com.github.tammo

import com.github.tammo.backend.JVMCodeGenerator
import com.github.tammo.frontend.`type`.TypeChecker
import com.github.tammo.frontend.`type`.TypedTree.CompilationUnit
import com.github.tammo.frontend.parse.AntlrParser

import java.nio.file.{Files, Paths}

object Main {

  def main(args: Array[String]): Unit = {
    val input = getInput
    val parser = AntlrParser
    val compilationUnit = parser.parse(input)
    val typedTree = TypeChecker.typeCheck(compilationUnit)
    val code =
      JVMCodeGenerator.generate(typedTree.asInstanceOf[CompilationUnit])

    code match
      case Right(value) =>
        writeCodeToFile(s"playground/${compilationUnit.fullyQualifiedName}.class", value)
      case Left(value) =>
        throw new RuntimeException(value)
  }

  private def getInput: String = {
    Files.readString(Paths.get("playground/test.fun"))
  }

  private def writeCodeToFile(fileName: String, code: Array[Byte]): Unit = {
    val path = Paths.get(fileName)
    Files.createDirectories(path.getParent)
    Files.write(path, code)
  }

}
