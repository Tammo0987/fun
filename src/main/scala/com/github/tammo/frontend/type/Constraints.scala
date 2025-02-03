package com.github.tammo.frontend.`type`

import com.github.tammo.diagnostics.PositionSpan
import com.github.tammo.frontend.`type`.TypedTree.*

import scala.annotation.tailrec

case class Constraint(left: Type, right: Type, span: PositionSpan)

object Constraints {

  def collect(typedTree: TypedTree): Seq[Constraint] = typedTree match {
    case compilationUnit: CompilationUnit =>
      collect(compilationUnit.classDeclaration)
    case classDeclaration: ClassDeclaration =>
      classDeclaration.effects.flatMap(collect) ++
        classDeclaration.functions.flatMap(collect)
    case effectDeclaration: EffectDeclaration =>
      effectDeclaration.parameters.flatMap(collect) ++ Seq(
        Constraint(
          unwrapFunctionReturnType(effectDeclaration.identifier.`type`),
          effectDeclaration.body.`type`,
          effectDeclaration.span
        )
      )
    case functionDeclaration: FunctionDeclaration =>
      functionDeclaration.parameters.flatMap(collect) ++ Seq(
        Constraint(
          unwrapFunctionReturnType(functionDeclaration.identifier.`type`),
          functionDeclaration.body.`type` ,
          functionDeclaration.span
        )
      )
    case expression: Expression  => collectConstraintsForExpression(expression)
    case _: NamespaceDeclaration => Seq.empty
    case NotImplemented          => Seq.empty
    case _: TypedIdentifier      => Seq.empty
    case _: Parameter            => Seq.empty
  }

  private def collectConstraintsForExpression(
      expression: Expression
  ): Seq[Constraint] = expression match
    case expression: ArithmeticExpression =>
      collectConstraintsForArithmeticExpression(expression)
    case term: Term     => collectConstraintsForTerm(term)
    case factor: Factor => collectConstraintsForFactor(factor)
    case pe: PrintExpression =>
      collect(pe.expression) ++ Seq(
        Constraint(pe.`type`, Type.Unit, pe.span)
      )
    case FunctionApplication(_, arguments, _) =>
      // TODO maybe add naming constraints into some type env? To recognize not found?
      arguments.flatMap(collect)
    case pe: ParenthesizedExpression =>
      Seq(Constraint(pe.`type`, pe.expression.`type`, pe.span))
    case _: StringLiteral => Seq.empty

  private def collectConstraintsForArithmeticExpression(
      expression: ArithmeticExpression
  ): Seq[Constraint] = expression match
    case operand: Operand =>
      collect(operand.left) ++ Seq(
        Constraint(operand.`type`, Type.Int, operand.span),
        Constraint(operand.left.`type`, Type.Int, operand.span)
      )
    case expression: BinaryArithmeticExpression =>
      collect(expression.left) ++ collect(expression.right) ++ Seq(
        Constraint(expression.`type`, Type.Int, expression.span),
        Constraint(expression.left.`type`, Type.Int, expression.span),
        Constraint(expression.right.`type`, Type.Int, expression.span)
      )

  private def collectConstraintsForTerm(
      term: Term
  ): Seq[Constraint] = term match
    case unaryTerm: UnaryTerm =>
      collect(unaryTerm.left) ++ Seq(
        Constraint(unaryTerm.`type`, Type.Int, unaryTerm.span),
        Constraint(unaryTerm.left.`type`, Type.Int, unaryTerm.span),
      )
    case binaryTerm: BinaryTerm =>
      collect(binaryTerm.left) ++ collect(binaryTerm.right) ++ Seq(
        Constraint(binaryTerm.`type`, Type.Int, binaryTerm.span),
        Constraint(binaryTerm.left.`type`, Type.Int, binaryTerm.span),
        Constraint(binaryTerm.right.`type`, Type.Int, binaryTerm.span)
      )

  private def collectConstraintsForFactor(
      factor: Factor
  ): Seq[Constraint] = factor match
    case _: IntLiteral => Seq.empty

  @tailrec
  private def unwrapFunctionReturnType(`type`: Type): Type = `type` match
    case Type.FunctionType(_, returnType) =>
      unwrapFunctionReturnType(returnType)
    case _ => `type`

}
