package com.github.tammo.frontend.parse

import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.{FunLexer, FunParser}
import SyntaxTree.CompilationUnit
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

object AntlrParser extends Parser {

  private val syntaxTreeBuilder = SyntaxTreeBuilder()

  override def parse(input: String): CompilationUnit = {
    val charStream = CharStreams.fromString(input)

    val lexer = FunLexer(charStream)
    val tokens = CommonTokenStream(lexer)

    val funParser = new FunParser(tokens)

    syntaxTreeBuilder
      .visitCompilationUnit(funParser.compilationUnit())
      .asInstanceOf[CompilationUnit]
  }

}
