package com.github.tammo.frontend.resolution

import com.github.tammo.frontend.ast.SyntaxTree

case class SymbolTable(scopes: Seq[Scope]) {

  def pushScope(treeContext: SyntaxTree): SymbolTable =
    copy(scopes =
      Scope(Map.empty, Some(treeContext), Some(currentScope)) +: scopes
    )

  def pushScope: SymbolTable =
    copy(scopes = Scope(Map.empty, None, Some(currentScope)) +: scopes)

  def popScope: SymbolTable =
    copy(scopes = scopes.tail)

  def define(symbol: Symbol): SymbolTable =
    copy(scopes = currentScope.define(symbol) +: scopes.tail)

  def lookup(name: String): Option[Symbol] =
    currentScope.lookup(name)

  def currentScope: Scope =
    scopes.headOption.getOrElse(Scope(Map.empty, None, None))

}

object SymbolTable {

  def globalScopedSymbolTable: SymbolTable = SymbolTable(Seq.empty).pushScope

}
