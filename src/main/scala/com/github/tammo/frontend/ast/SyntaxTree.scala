package com.github.tammo.frontend.ast

import com.github.tammo.diagnostics.PositionSpan

object SyntaxTree {

  sealed trait ExposeDeclaration extends SyntaxTree

  sealed trait Expression extends SyntaxTree

  sealed trait ArithmeticExpression extends Expression

  sealed trait Term extends Expression

  sealed trait Factor extends Expression

  case class NamespaceDeclaration(
      identifier: String,
      span: PositionSpan
  ) extends SyntaxTree

  case class CompilationUnit(
      namespace: Option[NamespaceDeclaration],
      useDeclarations: Seq[UseDeclaration],
      exposeDeclarations: Seq[ExposeDeclaration],
      classDeclaration: ClassDeclaration,
      span: PositionSpan
  ) extends SyntaxTree {
    def fullyQualifiedName: String =
      namespace
        .map(_.identifier)
        .map(i => s"$i/")
        .getOrElse("") + classDeclaration.name
  }

  case class UseDeclaration(
      identifiers: Seq[String],
      span: PositionSpan
  ) extends SyntaxTree

  case class ExposeNamespace(
      identifiers: Seq[String],
      span: PositionSpan
  ) extends ExposeDeclaration

  case class ExposeIdentifiers(
      identifiers: Seq[String],
      span: PositionSpan
  ) extends ExposeDeclaration

  case class ClassDeclaration(
      name: String,
      parameters: Seq[Parameter],
      effects: Seq[EffectDeclaration],
      functions: Seq[FunctionDeclaration],
      span: PositionSpan
  ) extends SyntaxTree

  case class EffectDeclaration(
      name: String,
      parameters: Seq[Parameter],
      returnType: String,
      body: Expression,
      span: PositionSpan
  ) extends SyntaxTree

  case class FunctionDeclaration(
      name: String,
      parameters: Seq[Parameter],
      returnType: String,
      body: Expression,
      span: PositionSpan
  ) extends SyntaxTree

  case class Parameter(
      identifier: String,
      `type`: String,
      span: PositionSpan
  ) extends SyntaxTree

  case class PrintExpression(
      expression: Expression,
      span: PositionSpan
  ) extends Expression

  case class StringLiteral(value: String, span: PositionSpan) extends Expression

  case class FunctionApplication(
      identifier: String,
      arguments: Seq[Expression],
      span: PositionSpan
  ) extends Expression

  case class Operand(left: Term, span: PositionSpan)
      extends ArithmeticExpression

  case class BinaryArithmeticExpression(
      left: Term,
      right: Expression,
      operator: ArithmeticExpression.Operator,
      span: PositionSpan
  ) extends ArithmeticExpression

  case class UnaryTerm(left: Factor, span: PositionSpan) extends Term

  case class BinaryTerm(
      left: Factor,
      right: Expression,
      operator: Term.Operator,
      span: PositionSpan
  ) extends Term

  case class IntLiteral(literal: Int, span: PositionSpan) extends Factor

  case class ParenthesizedExpression(
      expression: Expression,
      span: PositionSpan
  ) extends Expression

  object ArithmeticExpression {
    enum Operator {
      case ADD, SUBTRACT
    }
  }

  object Term {
    enum Operator {
      case MULTIPLY, DIVIDE
    }
  }

}

sealed trait SyntaxTree {

  def span: PositionSpan

}
