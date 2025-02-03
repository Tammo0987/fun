package com.github.tammo.diagnostics

import com.github.tammo.frontend.`type`.Type

sealed trait CompilerError {

  def message: String

  def level: Level

}

object CompilerError {

  sealed trait PositionedError extends CompilerError {
    def positionSpan: PositionSpan
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
  }

  sealed trait CodeGenerationError extends CompilerError {
    override def level: Level = Level.Fatal
  }

  case class MethodNotCreated(
      message: String
  ) extends CodeGenerationError

}
