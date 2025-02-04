package com.github.tammo

import com.github.tammo.backend.JVMCodeGenerator
import com.github.tammo.diagnostics.{CompilerErrorRenderer, SourceFile}
import com.github.tammo.frontend.`type`.TypeChecker
import com.github.tammo.frontend.`type`.TypedTree.CompilationUnit
import com.github.tammo.frontend.parse.AntlrParser
import com.github.tammo.frontend.resolution.{ReferenceChecker, SymbolTable}

import java.nio.file.{Files, Paths}

object Main {

  def main(args: Array[String]): Unit = {
    val input = getInput
    val parser = AntlrParser
    val compilationUnit = parser.parse(input)
    val result = for {
      _ <- ReferenceChecker.checkReferences(
        compilationUnit,
        SymbolTable.globalScopedSymbolTable
      )
      typedTree <- TypeChecker.typeCheck(compilationUnit)
      code <- JVMCodeGenerator.generate(typedTree.asInstanceOf[CompilationUnit])
    } yield code

    result match
      case Right(value) =>
        writeCodeToFile(
          s"playground/${compilationUnit.fullyQualifiedName}.class",
          value
        )
      case Left(compilerErrors) =>
        compilerErrors.foreach { compilerError =>
          println(
            CompilerErrorRenderer.render(
              compilerError,
              SourceFile(
                Paths.get("playground/test.fun").toAbsolutePath.toString,
                input
              )
            )
          )
        }
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
