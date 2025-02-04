package com.github.tammo.frontend.resolution

import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.diagnostics.CompilerError.FunctionOrEffectNotFound
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.ast.SyntaxTree.*
import com.github.tammo.frontend.resolution.Symbol.{FunctionSymbol, VariableSymbol}

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

    def map(f: SymbolTable => SymbolTable): SymbolTableWithErrors =
      copy(table = f(table))

    def flatMap(
        f: SymbolTable => SymbolTableWithErrors
    ): SymbolTableWithErrors =
      copy(table = f(table).table)

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
        case Some(_) =>
          table.appendError(FunctionOrEffectNotFound(functionApplication))
        case None =>
          table.appendError(FunctionOrEffectNotFound(functionApplication))
    case unit: CompilationUnit =>
      checkReferences(unit.classDeclaration, table)
    case declaration: ClassDeclaration =>
      val classSymbolTable = table
        .map(_.define(Symbol.ClassSymbol(declaration.name)))
        .map(_.pushScope)
      (declaration.effects ++ declaration.functions)
        .foldLeft[SymbolTableWithErrors](classSymbolTable) {
          (table, function) => checkReferences(function, table)
        }
        .map(_.popScope)
    case declaration: EffectDeclaration =>
      val effectSymbolTable =
        table.map(_.define(FunctionSymbol(declaration.name))).map(_.pushScope)
      val withParameters = declaration.parameters
        .foldLeft(effectSymbolTable) { (table, parameter) =>
          checkReferences(parameter, table)
        }
      val withBody = checkReferences(declaration.body, withParameters)
      withBody.map(_.popScope)
    case declaration: FunctionDeclaration =>
      val functionSymbolTable =
        table.map(_.define(FunctionSymbol(declaration.name))).map(_.pushScope)
      val withParameters =
        declaration.parameters.foldLeft(functionSymbolTable) {
          (table, parameter) => checkReferences(parameter, table)
        }
      val withBody = checkReferences(declaration.body, withParameters)
      withBody.map(_.popScope)
    case parameter: Parameter =>
      table.map(
        _.define(VariableSymbol(parameter.identifier))
      )
    case _ => table

}
