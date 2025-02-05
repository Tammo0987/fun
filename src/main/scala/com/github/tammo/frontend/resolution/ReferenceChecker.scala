package com.github.tammo.frontend.resolution

import com.github.tammo.core.{Semigroup, Validated}
import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.diagnostics.CompilerError.{EffectCalledInFunction, FunctionOrEffectNotFound}
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.ast.SyntaxTree.*
import com.github.tammo.frontend.resolution.Symbol.{ClassSymbol, EffectSymbol, FunctionSymbol}

object ReferenceChecker {

  // TODO add duplication again
  def checkReferences(
      syntaxTree: SyntaxTree,
      table: SymbolTable = SymbolTable.globalScopedSymbolTable
                     )(using
                       Semigroup[List[CompilerError]]
                     ): Validated[List[CompilerError], SymbolTable] = syntaxTree match
    case compilationUnit: CompilationUnit =>
      checkReferences(compilationUnit.classDeclaration, table)
    case declaration: ClassDeclaration =>
      val tableWithClass =
        table.define(ClassSymbol(declaration)).pushScope(declaration)

      val tableWithEffects = declaration.effects.foldLeft(tableWithClass) {
        (table, member) =>
          table.define(EffectSymbol(member))
      }
      val tableWithMembers = declaration.functions.foldLeft(tableWithEffects) {
        (table, member) =>
          table.define(FunctionSymbol(member))
      }
      val members = declaration.functions ++ declaration.effects
      members
        .map(checkReferences(_, tableWithMembers))
        .foldLeft(Validated.valid(tableWithMembers)) { (acc, member) =>
          acc.combine(member)((_, newTable) => newTable)
        }
        .map(_.popScope)
    case declaration: EffectDeclaration =>
      val tableWithEffect =
        table.define(EffectSymbol(declaration)).pushScope(declaration)
      val tableWithParameters = declaration.parameters
        .map(checkReferences(_, tableWithEffect))
        .foldLeft(Validated.valid(tableWithEffect))((acc, parameter) =>
          acc.combine(parameter)((_, newTable) => newTable)
        )

      tableWithParameters
        .flatMap(updatedTable =>
          tableWithParameters.combine(
            checkReferences(declaration.body, updatedTable)
          )((_, newSymbolTable) => newSymbolTable)
        )
        .map(_.popScope)
    case declaration: FunctionDeclaration =>
      val tableWithFunction =
        table.define(EffectSymbol(declaration)).pushScope(declaration)
      val tableWithParameters = declaration.parameters
        .map(checkReferences(_, tableWithFunction))
        .foldLeft(Validated.valid(tableWithFunction))((acc, parameter) =>
          acc.combine(parameter)((_, newTable) => newTable)
        )

      tableWithParameters
        .flatMap(updatedTable =>
          tableWithParameters.combine(
            checkReferences(declaration.body, updatedTable)
          )((_, newSymbolTable) => newSymbolTable)
        )
        .map(_.popScope)
    case application: FunctionApplication =>
      table.lookup(application.identifier) match
        case Some(_: FunctionSymbol) => Validated.valid(table)
        case Some(_: EffectSymbol) =>
          table.currentScope.treeContext match
            case Some(functionDeclaration: FunctionDeclaration) =>
              Validated.invalid(
                List(EffectCalledInFunction(functionDeclaration, application))
              )
            case _ => Validated.valid(table)
        case _ => Validated.invalid(List(FunctionOrEffectNotFound(application)))
    case _ => Validated.valid(table)

}
