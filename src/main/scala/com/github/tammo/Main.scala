package com.github.tammo

import com.github.tammo.backend.JVMCodeGenerator
import com.github.tammo.frontend.parse.AntlrParser
import com.github.tammo.frontend.`type`.TypeChecker
import com.github.tammo.frontend.`type`.TypedTree.CompilationUnit

import java.nio.file.{Files, Paths}

object Main {

  def main(args: Array[String]): Unit = {
    val input = getInput
    val parser = AntlrParser
    val compilationUnit = parser.parse(input)
    val typedTree = TypeChecker.typeCheck(compilationUnit)
    val code =
      JVMCodeGenerator.generate(typedTree.asInstanceOf[CompilationUnit])
    writeCodeToFile(s"${compilationUnit.fullyQualifiedName}.class", code)
  }

  private def getInput: String = {
    Files.readString(Paths.get("test.fun"))
  }

  private def writeCodeToFile(fileName: String, code: Array[Byte]): Unit = {
    val path = Paths.get(fileName)
    Files.createDirectories(path.getParent)
    Files.write(path, code)
  }

}
