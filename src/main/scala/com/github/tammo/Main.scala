package com.github.tammo

import com.github.tammo.backend.JVMCodeGenerator
import com.github.tammo.core.{Invalid, SourceFile, Valid}
import com.github.tammo.diagnostics.CompilerErrorRenderer
import com.github.tammo.frontend.InputPhase
import com.github.tammo.frontend.`type`.{TypeChecker, TypedTree}
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.parse.ParserPhase
import com.github.tammo.frontend.resolution.SemanticAnalysesPhase

import java.nio.file.{Files, Paths}

object Main {

  def main(args: Array[String]): Unit = {
    val compilationResult = for {
      sourceFile <- InputPhase.run("playground/test.fun")
      compilationUnit <- ParserPhase.run(sourceFile)
      _ <- SemanticAnalysesPhase.run(compilationUnit)
    } yield compilationUnit

    val inputSourceFile = getInput("playground/test.fun")

    compilationResult match
      case Valid(compilationUnit) =>
        val result = for {
          typedTree <- TypeChecker.typeCheck(compilationUnit)
          code <- JVMCodeGenerator.generate(
            typedTree.asInstanceOf[TypedTree.CompilationUnit]
          )
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
                  inputSourceFile
                )
              )
            }
      case Invalid(errors) =>
        errors.foreach { compilerError =>
          println(
            CompilerErrorRenderer.render(
              compilerError,
              inputSourceFile
            )
          )
        }

  }

  private def getInput(location: String): SourceFile = {
    val path = Paths.get(location)
    val sourceId = path.toAbsolutePath.toString
    SourceFile(sourceId, Files.readString(path))
  }

  private def writeCodeToFile(fileName: String, code: Array[Byte]): Unit = {
    val path = Paths.get(fileName)
    Files.createDirectories(path.getParent)
    Files.write(path, code)
  }

}
