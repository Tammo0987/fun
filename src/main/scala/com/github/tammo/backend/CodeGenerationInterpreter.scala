package com.github.tammo.backend

import com.github.tammo.backend.CodeGenerationAction.{BeginMethod, ClassAction, EndMethod, MethodAction}
import org.objectweb.asm.{ClassWriter, MethodVisitor}

object CodeGenerationInterpreter {

  // TODO change to correct error type
  def interpret(
      actions: Seq[CodeGenerationAction],
      classWriter: ClassWriter
  ): Either[String, Unit] = {
    val result =
      actions.foldLeft[(Either[String, Unit], Option[MethodVisitor])](
        (Right(()), None)
      ) { (acc, action) =>
        interpretAction(action, classWriter, acc._2)
      }

    result._1
  }

  private def interpretAction(
      action: CodeGenerationAction,
      classWriter: ClassWriter,
      methodVisitor: Option[MethodVisitor]
  ): (Either[String, Unit], Option[MethodVisitor]) = action match
    case ClassAction(f) => (Right(f(classWriter)), methodVisitor)
    case BeginMethod(access, name, descriptor) =>
      val newMethodVisitor = classWriter.visitMethod(
        access,
        name,
        descriptor,
        null,
        new Array[String](0)
      )
      newMethodVisitor.visitCode()
      (Right(()), Some(newMethodVisitor))
    case MethodAction(f) =>
      methodVisitor match
        case None    => (Left("No method visitor"), methodVisitor)
        case Some(_) => (Right(f(methodVisitor.get)), methodVisitor)
    case EndMethod =>
      methodVisitor.foreach { mv =>
        mv.visitMaxs(0, 0)
        mv.visitEnd()
      }
      (Right(()), None)

}
