package com.github.tammo.backend

import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.frontend.`type`.TypedTree.CompilationUnit

trait CodeGenerator {

  def generate(
      compilationUnit: CompilationUnit
  ): Either[CompilerError, Array[Byte]]

}
