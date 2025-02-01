package com.github.tammo.frontend.ast

object SyntaxTree {

  case class NamespaceDeclaration(identifier: String) extends SyntaxTree

  case class CompilationUnit(
      namespace: Option[NamespaceDeclaration],
      useDeclarations: Seq[UseDeclaration],
      exposeDeclarations: Seq[ExposeDeclaration],
      classDeclaration: ClassDeclaration
  ) extends SyntaxTree {
    def fullyQualifiedName: String =
      namespace.map(_.identifier).map(i => s"$i/").getOrElse("") + classDeclaration.name
  }

  case class UseDeclaration(identifiers: Seq[String]) extends SyntaxTree

  sealed trait ExposeDeclaration extends SyntaxTree

  case class ExposeNamespace(identifiers: Seq[String]) extends ExposeDeclaration

  case class ExposeIdentifiers(identifiers: Seq[String])
      extends ExposeDeclaration

  case class ClassDeclaration(
      name: String,
      parameters: Seq[Parameter],
      effects: Seq[EffectDeclaration],
      functions: Seq[FunctionDeclaration]
  ) extends SyntaxTree

  case class EffectDeclaration(
      name: String,
      parameters: Seq[Parameter],
      returnType: String,
      body: Expression
  ) extends SyntaxTree

  case class FunctionDeclaration(
      name: String,
      parameters: Seq[Parameter],
      returnType: String,
      body: Expression
  ) extends SyntaxTree

  case class Parameter(identifier: String, `type`: String) extends SyntaxTree

  sealed trait Expression extends SyntaxTree

  case class PrintExpression(expression: Expression) extends Expression

  case class StringLiteral(value: String) extends Expression

  case class FunctionApplication(
      identifier: String,
      arguments: Seq[Expression]
  ) extends Expression

  sealed trait ArithmeticExpression extends Expression

  object ArithmeticExpression {
    enum Operator {
      case ADD, SUBTRACT
    }
  }

  case class Operand(left: Term) extends ArithmeticExpression

  case class BinaryArithmeticExpression(
      left: Term,
      right: Expression,
      operator: ArithmeticExpression.Operator
  ) extends ArithmeticExpression

  sealed trait Term extends Expression

  object Term {
    enum Operator {
      case MULTIPLY, DIVIDE
    }
  }

  case class UnaryTerm(left: Factor) extends Term

  case class BinaryTerm(
      left: Factor,
      right: Expression,
      operator: Term.Operator
  ) extends Term

  sealed trait Factor extends Expression

  case class IntLiteral(literal: Int) extends Factor

  case class ParenthesizedExpression(expression: Expression) extends Expression

}

sealed trait SyntaxTree
