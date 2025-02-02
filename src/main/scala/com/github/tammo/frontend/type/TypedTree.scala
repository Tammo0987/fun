package com.github.tammo.frontend.`type`

sealed trait TypedTree

object TypedTree {

  sealed trait Expression extends TypedTree {
    def `type`: Type
  }

  sealed trait ArithmeticExpression extends Expression

  sealed trait Term extends Expression

  sealed trait Factor extends Expression

  case class NamespaceDeclaration(identifier: String) extends TypedTree

  case class CompilationUnit(
      namespace: Option[NamespaceDeclaration],
      classDeclaration: ClassDeclaration
  ) extends TypedTree {

    def fullyQualifiedName: String =
      namespace
        .map(_.identifier)
        .map(i => s"$i/")
        .getOrElse("") + classDeclaration.name

  }

  case class ClassDeclaration(
      name: String,
      effects: Seq[EffectDeclaration],
      functions: Seq[FunctionDeclaration]
  ) extends TypedTree

  case class TypedIdentifier(name: String, `type`: Type) extends TypedTree

  case class EffectDeclaration(
      identifier: TypedIdentifier,
      parameters: Seq[Parameter],
      body: Expression
  ) extends TypedTree

  case class FunctionDeclaration(
      identifier: TypedIdentifier,
      parameters: Seq[Parameter],
      body: Expression
  ) extends TypedTree

  case class Parameter(identifier: TypedIdentifier) extends TypedTree

  case class PrintExpression(expression: Expression) extends Expression {
    override def `type`: Type = Type.Unit
  }

  case class StringLiteral(value: String) extends Expression {
    override def `type`: Type = Type.String
  }

  case class FunctionApplication(
      identifier: TypedIdentifier,
      arguments: Seq[Expression]
  ) extends Expression {
    override def `type`: Type = {
      arguments.foldRight(identifier.`type`) { (arg, acc) =>
        Type.FunctionType(arg.`type`, acc)
      }
    }
  }

  case class Operand(`type`: Type, left: Term) extends ArithmeticExpression

  case class BinaryArithmeticExpression(
      `type`: Type,
      left: Term,
      right: Expression,
      operator: ArithmeticExpression.Operator
  ) extends ArithmeticExpression

  case class UnaryTerm(`type`: Type, left: Factor) extends Term

  case class BinaryTerm(
      `type`: Type,
      left: Factor,
      right: Expression,
      operator: Term.Operator
  ) extends Term

  case class IntLiteral(literal: Int) extends Factor {
    override def `type`: Type = Type.Int
  }

  case class ParenthesizedExpression(expression: Expression)
      extends Expression {
    override def `type`: Type = expression.`type`
  }

  object NotImplemented extends TypedTree

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
