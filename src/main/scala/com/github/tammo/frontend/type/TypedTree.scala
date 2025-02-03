package com.github.tammo.frontend.`type`

import com.github.tammo.diagnostics.PositionSpan

sealed trait TypedTree {

  def span: PositionSpan

}

object TypedTree {

  sealed trait Expression extends TypedTree {
    def `type`: Type
  }

  sealed trait ArithmeticExpression extends Expression

  sealed trait Term extends Expression

  sealed trait Factor extends Expression

  case class NamespaceDeclaration(identifier: String, span: PositionSpan)
      extends TypedTree

  case class CompilationUnit(
      namespace: Option[NamespaceDeclaration],
      classDeclaration: ClassDeclaration,
      span: PositionSpan
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
      functions: Seq[FunctionDeclaration],
      span: PositionSpan
  ) extends TypedTree

  case class TypedIdentifier(name: String, `type`: Type, span: PositionSpan)
      extends TypedTree

  case class EffectDeclaration(
      identifier: TypedIdentifier,
      parameters: Seq[Parameter],
      body: Expression,
      span: PositionSpan
  ) extends TypedTree

  case class FunctionDeclaration(
      identifier: TypedIdentifier,
      parameters: Seq[Parameter],
      body: Expression,
      span: PositionSpan
  ) extends TypedTree

  case class Parameter(identifier: TypedIdentifier, span: PositionSpan)
      extends TypedTree

  case class PrintExpression(expression: Expression, span: PositionSpan)
      extends Expression {
    override def `type`: Type = Type.Unit
  }

  case class StringLiteral(value: String, span: PositionSpan)
      extends Expression {
    override def `type`: Type = Type.String
  }

  case class FunctionApplication(
      identifier: TypedIdentifier,
      arguments: Seq[Expression],
      span: PositionSpan
  ) extends Expression {
    override def `type`: Type = {
      arguments.foldRight(identifier.`type`) { (arg, acc) =>
        Type.FunctionType(arg.`type`, acc)
      }
    }
  }

  case class Operand(`type`: Type, left: Term, span: PositionSpan)
      extends ArithmeticExpression

  case class BinaryArithmeticExpression(
      `type`: Type,
      left: Term,
      right: Expression,
      operator: ArithmeticExpression.Operator,
      span: PositionSpan
  ) extends ArithmeticExpression

  case class UnaryTerm(`type`: Type, left: Factor, span: PositionSpan)
      extends Term

  case class BinaryTerm(
      `type`: Type,
      left: Factor,
      right: Expression,
      operator: Term.Operator,
      span: PositionSpan
  ) extends Term

  case class IntLiteral(literal: Int, span: PositionSpan) extends Factor {
    override def `type`: Type = Type.Int
  }

  case class ParenthesizedExpression(expression: Expression, span: PositionSpan)
      extends Expression {
    override def `type`: Type = expression.`type`
  }

  object NotImplemented extends TypedTree {
    override def span: PositionSpan = PositionSpan("", 0, 0)
  }

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
