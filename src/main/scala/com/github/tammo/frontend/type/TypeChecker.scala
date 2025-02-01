package com.github.tammo.frontend.`type`

import com.github.tammo.frontend.ast.SyntaxTree

object TypeChecker {

  def typeCheck(tree: SyntaxTree.CompilationUnit): TypedTree = {
    val annotatedTypeTree = TypeAnnotate.annotateTypes(tree)
    val constraints = Constraints.collect(annotatedTypeTree)
    val substitutions = Unifier.unifyAll(constraints)
    SubstitutionApplier.applySubstitutions(
      annotatedTypeTree,
      `type` => Unifier.applySubstitution(substitutions, `type`)
    )
  }
}
