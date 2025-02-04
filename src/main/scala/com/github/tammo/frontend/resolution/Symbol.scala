package com.github.tammo.frontend.resolution

sealed trait Symbol {
  def name: String
}

object Symbol {

  case class ClassSymbol(name: String) extends Symbol

  case class FunctionSymbol(name: String) extends Symbol

  case class VariableSymbol(name: String) extends Symbol

}
