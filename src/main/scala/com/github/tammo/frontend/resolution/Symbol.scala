package com.github.tammo.frontend.resolution

import com.github.tammo.frontend.ast.SyntaxTree.Declaration

sealed trait Symbol {
  def name: String = declaration.identifier

  def declaration: Declaration
}

object Symbol {

  case class ClassSymbol(declaration: Declaration) extends Symbol

  case class FunctionSymbol(declaration: Declaration) extends Symbol

  case class EffectSymbol(declaration: Declaration) extends Symbol

  case class VariableSymbol(declaration: Declaration) extends Symbol

}
