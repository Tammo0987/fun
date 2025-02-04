package com.github.tammo.frontend.parse

import com.github.tammo.diagnostics.SourceFile
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.ast.SyntaxTree.CompilationUnit
import com.github.tammo.{FunLexer, FunParser}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

object AntlrParser extends Parser {

  override def parse(sourceFile: SourceFile): CompilationUnit = {
    val charStream = CharStreams.fromString(sourceFile.content)

    val lexer = FunLexer(charStream)
    val tokens = CommonTokenStream(lexer)

    val funParser = new FunParser(tokens)

    val syntaxTreeBuilder = SyntaxTreeBuilder(sourceFile)

    syntaxTreeBuilder
      .visitCompilationUnit(funParser.compilationUnit())
      .asInstanceOf[CompilationUnit]
  }

}
