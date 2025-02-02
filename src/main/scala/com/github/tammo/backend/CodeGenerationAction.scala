package com.github.tammo.backend

import org.objectweb.asm.{ClassWriter, MethodVisitor}

sealed trait CodeGenerationAction {}

object CodeGenerationAction {

  case class ClassAction(
      f: ClassWriter => Unit
  ) extends CodeGenerationAction

  case class BeginMethod(
      access: Int,
      name: String,
      descriptor: String
  ) extends CodeGenerationAction

  case class MethodAction(
      f: MethodVisitor => Unit
  ) extends CodeGenerationAction

  case object EndMethod extends CodeGenerationAction

}
