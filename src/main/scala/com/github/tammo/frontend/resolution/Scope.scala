package com.github.tammo.frontend.resolution

case class Scope(symbols: Map[String, Symbol], parent: Option[Scope]) {

  def lookup(name: String): Option[Symbol] = {
    symbols.get(name).orElse(parent.flatMap(_.lookup(name)))
  }

  def define(symbol: Symbol): Scope =
    copy(symbols = symbols + (symbol.name -> symbol))

}
