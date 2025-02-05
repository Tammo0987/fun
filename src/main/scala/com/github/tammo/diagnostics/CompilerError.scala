package com.github.tammo.diagnostics

import com.github.tammo.frontend.`type`.Type
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.ast.SyntaxTree.{Declaration, FunctionApplication, FunctionDeclaration}

sealed trait CompilerError {

  def message: String

  def level: Level

}

object CompilerError {

  sealed trait PositionedError extends CompilerError {
    def positions: Iterable[PositionSpan]
  }

  sealed trait ParseError extends CompilerError

  sealed trait TypeCheckError extends CompilerError

  case class CyclicTypeReferenceError(
      leftType: Type,
      rightType: Type
  ) extends TypeCheckError {
    override def message: String =
      s"Cannot unify: The type variable $leftType occurs within type $rightType, resulting in an infinite type."

    override def level: Level = Level.Error
  }

  case class IncompatibleTypesError(
      leftType: Type,
      rightType: Type,
      positionSpan: PositionSpan
  ) extends TypeCheckError
      with PositionedError {
    override def message: String =
      s"Type $leftType is incompatible with type $rightType."

    override def level: Level = Level.Error

    override def positions: Iterable[PositionSpan] = Some(positionSpan)
  }

  case class FunctionOrEffectNotFound(functionApplication: FunctionApplication)
      extends CompilerError
      with PositionedError {
    override def message: String =
      s"Function or effect call \"${functionApplication.identifier}\" not found. Did you forget to import it?"

    override def level: Level = Level.Error

    override def positions: Iterable[PositionSpan] = Some(
      functionApplication.span
    )
  }

  case class EffectCalledInFunction(
      functionDeclaration: FunctionDeclaration,
      effectApplication: FunctionApplication
  ) extends CompilerError
      with PositionedError {
    override def message: String =
      s"The effect \"${effectApplication.identifier}\" can't be used in the function \"${functionDeclaration.identifier}\". Effects can only be used in other effects."

    override def level: Level = Level.Error

    override def positions: Iterable[PositionSpan] = Some(
      effectApplication.span
    )
  }

  case class DuplicateDeclaration(left: Declaration, right: Declaration)
      extends CompilerError
      with PositionedError {
    override def message: String =
      s"Detected duplicate declaration for \"${left.identifier}\"."

    override def level: Level = Level.Error

    override def positions: Iterable[PositionSpan] = Seq(left.span, right.span)
  }

  sealed trait CodeGenerationError extends CompilerError {
    override def level: Level = Level.Fatal
  }

  case class MethodNotCreated(
      message: String
  ) extends CodeGenerationError

}
