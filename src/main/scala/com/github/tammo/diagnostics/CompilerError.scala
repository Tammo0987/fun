package com.github.tammo.diagnostics

import com.github.tammo.frontend.`type`.Type

sealed trait CompilerError {

  def message: String

  def level: Level

}

object CompilerError {

  sealed trait ParseError extends CompilerError

  sealed trait TypeCheckError extends CompilerError

  sealed trait CodeGenerationError extends CompilerError {
    override def level: Level = Level.Fatal
  }

  case class CyclicTypeReferenceError(
      firstType: Type,
      secondType: Type
  ) extends TypeCheckError {
    override def message: String =
      s"Cannot unify: The type variable $firstType occurs within type $secondType, resulting in an infinite type."

    override def level: Level = Level.Error
  }

  case class MethodNotCreated(
      message: String
  ) extends CodeGenerationError

}
