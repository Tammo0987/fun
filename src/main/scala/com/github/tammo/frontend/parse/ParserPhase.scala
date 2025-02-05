package com.github.tammo.frontend.parse

import com.github.tammo.{FunLexer, FunParser}
import com.github.tammo.core.{Phase, SourceFile, Validated}
import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.frontend.ast.SyntaxTree.CompilationUnit
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

object ParserPhase extends Phase[SourceFile, CompilationUnit] {

  override def run(
      input: SourceFile
  ): Validated[List[CompilerError], CompilationUnit] = {
    val charStream = CharStreams.fromString(input.content)

    val lexer = FunLexer(charStream)
    val tokens = CommonTokenStream(lexer)

    val funParser = new FunParser(tokens)

    val syntaxTreeBuilder = SyntaxTreeBuilder(input)

    Validated.valid(
      syntaxTreeBuilder
        .visitCompilationUnit(funParser.compilationUnit())
        .asInstanceOf[CompilationUnit]
    )
  }
}
