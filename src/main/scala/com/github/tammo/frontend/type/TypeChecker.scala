package com.github.tammo.frontend.`type`

import com.github.tammo.diagnostics.CompilerError.TypeCheckError
import com.github.tammo.frontend.ast.SyntaxTree

object TypeChecker {

  def typeCheck(tree: SyntaxTree.CompilationUnit): Either[Seq[TypeCheckError], TypedTree] = {
    val annotatedTypeTree = TypeAnnotate.annotateTypes(tree)
    val constraints = Constraints.collect(annotatedTypeTree)
    Unifier.unifyAll(constraints).map { substitutions =>
      SubstitutionApplier.applySubstitutions(
        annotatedTypeTree,
        `type` => Unifier.applySubstitution(substitutions, `type`)
      )
    }
  }
}
