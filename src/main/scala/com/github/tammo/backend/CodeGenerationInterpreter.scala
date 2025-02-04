package com.github.tammo.backend

import com.github.tammo.backend.CodeGenerationAction.{BeginMethod, ClassAction, EndMethod, MethodAction}
import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.diagnostics.CompilerError.{CodeGenerationError, MethodNotCreated}
import org.objectweb.asm.{ClassWriter, MethodVisitor}

object CodeGenerationInterpreter {

  def interpret(
      actions: Seq[CodeGenerationAction],
      classWriter: ClassWriter
  ): Either[Seq[CodeGenerationError], Unit] = {
    val result =
      actions
        .foldLeft[(Either[Seq[CodeGenerationError], Unit], Option[MethodVisitor])](
          (Right(()), None)
        ) {
          case ((_: Right[Seq[CodeGenerationError], Unit], methodVisitor), action) =>
            interpretAction(action, classWriter, methodVisitor)
          case ((compilerErrors: Left[Seq[CodeGenerationError], Unit], methodVisitor), _) =>
            (compilerErrors, methodVisitor)
        }

    result._1
  }

  private def interpretAction(
      action: CodeGenerationAction,
      classWriter: ClassWriter,
      methodVisitor: Option[MethodVisitor]
  ): (Either[Seq[CodeGenerationError], Unit], Option[MethodVisitor]) = action match
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
        case None =>
          (
            Left(
              Seq(
                MethodNotCreated(
                  "Tried to generate code for a method, but method never started."
                )
              )
            ),
            methodVisitor
          )
        case Some(_) => (Right(f(methodVisitor.get)), methodVisitor)
    case EndMethod =>
      methodVisitor.foreach { mv =>
        mv.visitMaxs(0, 0)
        mv.visitEnd()
      }
      (Right(()), None)

}
