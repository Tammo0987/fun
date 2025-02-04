package com.github.tammo.frontend.resolution

case class SymbolTable(scopes: Seq[Scope]) {

  def pushScope: SymbolTable =
    copy(scopes = Scope(Map.empty, Some(currentScope)) +: scopes)

  def popScope: SymbolTable =
    copy(scopes = scopes.tail)

  def define(symbol: Symbol): SymbolTable =
    copy(scopes = currentScope.define(symbol) +: scopes.tail)

  def lookup(name: String): Option[Symbol] =
    currentScope.lookup(name)

  private def currentScope: Scope =
    scopes.headOption.getOrElse(Scope(Map.empty, None))

}

object SymbolTable {

  def globalScopedSymbolTable: SymbolTable = SymbolTable(Seq.empty).pushScope

}
