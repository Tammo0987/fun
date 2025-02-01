package com.github.tammo.frontend.parse

import com.github.tammo.frontend.ast.SyntaxTree.CompilationUnit

trait Parser {
  
  def parse(input: String): CompilationUnit
  
}
