package com.github.tammo.frontend.resolution

import com.github.tammo.frontend.ast.SyntaxTree

case class Scope(
    symbols: Map[String, Symbol],
    treeContext: Option[SyntaxTree],
    parent: Option[Scope]
) {

  def lookup(name: String): Option[Symbol] = {
    symbols.get(name).orElse(parent.flatMap(_.lookup(name)))
  }

  def define(symbol: Symbol): Scope =
    copy(symbols = symbols + (symbol.name -> symbol))

}
