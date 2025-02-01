package com.github.tammo.backend

import com.github.tammo.frontend.`type`.TypedTree.CompilationUnit

trait CodeGenerator {

  def generate(compilationUnit: CompilationUnit): Array[Byte]

}
