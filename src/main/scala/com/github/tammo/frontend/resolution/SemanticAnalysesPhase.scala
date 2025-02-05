package com.github.tammo.frontend.resolution

import com.github.tammo.core.{Phase, Semigroup, Validated}
import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.frontend.ast.SyntaxTree.CompilationUnit

object SemanticAnalysesPhase extends Phase[CompilationUnit, SymbolTable] {

  override def run(
      input: CompilationUnit
  ): Validated[List[CompilerError], SymbolTable] =
    ReferenceChecker.checkReferences(input)

  private given listSemigroup[T]: Semigroup[List[T]] =
    (x: List[T], y: List[T]) => x ++ y

}
