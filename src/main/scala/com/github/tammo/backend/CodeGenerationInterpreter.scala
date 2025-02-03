package com.github.tammo.backend

import com.github.tammo.backend.CodeGenerationAction.{BeginMethod, ClassAction, EndMethod, MethodAction}
import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.diagnostics.CompilerError.MethodNotCreated
import org.objectweb.asm.{ClassWriter, MethodVisitor}

object CodeGenerationInterpreter {

  // TODO change to correct error type
  def interpret(
      actions: Seq[CodeGenerationAction],
      classWriter: ClassWriter
  ): Either[CompilerError, Unit] = {
    val result =
      actions.foldLeft[(Either[CompilerError, Unit], Option[MethodVisitor])](
        (Right(()), None)
      ) {
        case ((_: Right[CompilerError, Unit], methodVisitor), action) =>
          interpretAction(action, classWriter, methodVisitor)
        case ((compilerError: Left[CompilerError, Unit], methodVisitor), _) =>
          (compilerError, methodVisitor)
      }

    result._1
  }

  private def interpretAction(
      action: CodeGenerationAction,
      classWriter: ClassWriter,
      methodVisitor: Option[MethodVisitor]
  ): (Either[CompilerError, Unit], Option[MethodVisitor]) = action match
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
              MethodNotCreated(
                "Tried to generate code for a method, but method never started."
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
