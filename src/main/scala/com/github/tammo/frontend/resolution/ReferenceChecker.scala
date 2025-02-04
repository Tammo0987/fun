package com.github.tammo.frontend.resolution

import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.diagnostics.CompilerError.{DuplicateDeclaration, EffectCalledInFunction, FunctionOrEffectNotFound}
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.ast.SyntaxTree.*
import com.github.tammo.frontend.resolution.Symbol.{ClassSymbol, EffectSymbol, FunctionSymbol, VariableSymbol}

object ReferenceChecker {

  def checkReferences(
      syntaxTree: SyntaxTree,
      table: SymbolTable
  ): Either[Seq[CompilerError], SymbolTable] =
    val result =
      checkReferences(syntaxTree, SymbolTableWithErrors(table, Seq.empty))
    if (result.errors.isEmpty) {
      Right(result.table)
    } else {
      Left(result.errors)
    }

  private case class SymbolTableWithErrors(
      table: SymbolTable,
      errors: Seq[CompilerError]
  ) {

    def defineUnique(symbol: Symbol): SymbolTableWithErrors = {
      table.lookup(symbol.name) match
        case Some(definedSymbol) =>
          appendError(
            DuplicateDeclaration(definedSymbol.declaration, symbol.declaration)
          )
        case None => map(_.define(symbol))
    }

    def map(f: SymbolTable => SymbolTable): SymbolTableWithErrors =
      copy(table = f(table))

    def appendError(error: CompilerError): SymbolTableWithErrors =
      copy(errors = errors :+ error)
  }

  private def checkReferences(
      syntaxTree: SyntaxTree,
      table: SymbolTableWithErrors
  ): SymbolTableWithErrors = syntaxTree match
    case functionApplication: FunctionApplication =>
      table.table.lookup(
        functionApplication.identifier
      ) match
        case Some(_: FunctionSymbol) => table
        case Some(_: EffectSymbol) =>
          table.table.currentScope.treeContext match
            case Some(functionDeclaration: FunctionDeclaration) =>
              table.appendError(
                EffectCalledInFunction(functionDeclaration, functionApplication)
              )
            case _ => table
        case Some(_) =>
          table.appendError(FunctionOrEffectNotFound(functionApplication))
        case None =>
          table.appendError(FunctionOrEffectNotFound(functionApplication))
    case unit: CompilationUnit =>
      checkReferences(unit.classDeclaration, table)
    case declaration: ClassDeclaration =>
      val classSymbolTable = table
        .defineUnique(ClassSymbol(declaration))
        .map(_.pushScope(declaration))
      (declaration.effects ++ declaration.functions)
        .foldLeft[SymbolTableWithErrors](classSymbolTable) {
          (table, function) => checkReferences(function, table)
        }
        .map(_.popScope)
    case declaration: EffectDeclaration =>
      val effectSymbolTable =
        table
          .defineUnique(EffectSymbol(declaration))
          .map(_.pushScope(declaration))
      val withParameters = declaration.parameters
        .foldLeft(effectSymbolTable) { (table, parameter) =>
          checkReferences(parameter, table)
        }
      val withBody = checkReferences(declaration.body, withParameters)
      withBody.map(_.popScope)
    case declaration: FunctionDeclaration =>
      val functionSymbolTable =
        table
          .defineUnique(FunctionSymbol(declaration))
          .map(_.pushScope(declaration))
      val withParameters =
        declaration.parameters.foldLeft(functionSymbolTable) {
          (table, parameter) => checkReferences(parameter, table)
        }
      val withBody = checkReferences(declaration.body, withParameters)
      withBody.map(_.popScope)
    case parameter: Parameter =>
      table.defineUnique(VariableSymbol(parameter))
    case _ => table

}
