package com.github.tammo.backend

import TypedTreeWriter.MethodWriter
import com.github.tammo.frontend.`type`.TypedTree.{ArithmeticExpression, Term}
import com.github.tammo.frontend.`type`.{Type, TypedTree, TypedTreeVisitor}
import org.objectweb.asm.{ClassWriter, MethodVisitor}
import org.objectweb.asm.Opcodes.*

import scala.annotation.tailrec

class TypedTreeWriter(
    private val classWriter: ClassWriter,
    private val fullyQualifiedClassName: String
) extends TypedTreeVisitor.Visitor {

  def toByteCode: Array[Byte] = classWriter.toByteArray

  override def visitCompilationUnit(
      compilationUnit: TypedTree.CompilationUnit
  ): Unit = {
    compilationUnit.namespace.foreach(visitNamespaceDeclaration)
    visitClassDeclaration(compilationUnit.classDeclaration)
  }

  override def visitClassDeclaration(
      classDeclaration: TypedTree.ClassDeclaration
  ): Unit = {
    classWriter.visit(
      V17,
      ACC_PUBLIC,
      fullyQualifiedClassName,
      null,
      "java/lang/Object",
      new Array[String](0)
    )

    classDeclaration.effects.foreach(visitEffectDeclaration)
    classDeclaration.functions.foreach(visitFunctionDeclaration)

    classWriter.visitEnd()
  }

  override def visitEffectDeclaration(
      effect: TypedTree.EffectDeclaration
  ): Unit = {
    val descriptor =
      TypeMapper.mapTypeAsFunctionTypeToBytecodeString(
        effect.identifier.`type`
      )
    val methodVisitor = classWriter.visitMethod(
      ACC_PUBLIC | ACC_STATIC,
      effect.identifier.name,
      descriptor,
      null,
      new Array[String](0)
    )
    methodVisitor.visitCode()

    MethodWriter(methodVisitor).visitExpression(effect.body)

    @tailrec
    def writeReturn(returnType: Type): Unit = {
      returnType match
        case Type.Unit        => methodVisitor.visitInsn(RETURN)
        case Type.Int         => methodVisitor.visitInsn(IRETURN)
        case Type.Boolean     => methodVisitor.visitInsn(IRETURN)
        case Type.String      => methodVisitor.visitInsn(ARETURN)
        case Type.StringArray => methodVisitor.visitInsn(ARETURN)
        case Type.FunctionType(_, returnType) =>
          writeReturn(returnType)
        case Type.Variable() =>
          throw new IllegalStateException("Unexpected type variable")
    }

    writeReturn(effect.identifier.`type`)

    methodVisitor.visitEnd()
    methodVisitor.visitMaxs(0, 0)
  }

  override def visitFunctionDeclaration(
      function: TypedTree.FunctionDeclaration
  ): Unit = {
    val descriptor =
      TypeMapper.mapTypeAsFunctionTypeToBytecodeString(
        function.identifier.`type`
      )
    val methodVisitor = classWriter.visitMethod(
      ACC_PUBLIC | ACC_STATIC,
      function.identifier.name,
      descriptor,
      null,
      new Array[String](0)
    )
    methodVisitor.visitCode()

    MethodWriter(methodVisitor).visitExpression(function.body)

    @tailrec
    def writeReturn(returnType: Type): Unit = {
      returnType match
        case Type.Unit        => methodVisitor.visitInsn(RETURN)
        case Type.Int         => methodVisitor.visitInsn(IRETURN)
        case Type.Boolean     => methodVisitor.visitInsn(IRETURN)
        case Type.String      => methodVisitor.visitInsn(ARETURN)
        case Type.StringArray => methodVisitor.visitInsn(ARETURN)
        case Type.FunctionType(_, returnType) =>
          writeReturn(returnType)
        case Type.Variable() =>
          throw new IllegalStateException("Unexpected type variable")
    }

    writeReturn(function.identifier.`type`)

    methodVisitor.visitEnd()
    methodVisitor.visitMaxs(0, 0)
  }

}

object TypedTreeWriter {

  class MethodWriter(private val methodVisitor: MethodVisitor)
      extends TypedTreeVisitor.Visitor {

    override def visitOperand(operand: TypedTree.Operand): Unit =
      visitTerm(operand.left)

    override def visitBinaryArithmeticExpression(
        binaryArithmeticExpression: TypedTree.BinaryArithmeticExpression
    ): Unit = {
      visitTerm(binaryArithmeticExpression.left)
      visitExpression(binaryArithmeticExpression.right)
      binaryArithmeticExpression.operator match
        case ArithmeticExpression.Operator.ADD => methodVisitor.visitInsn(IADD)
        case ArithmeticExpression.Operator.SUBTRACT =>
          methodVisitor.visitInsn(ISUB)
    }

    override def visitUnaryTerm(unaryTerm: TypedTree.UnaryTerm): Unit =
      visitFactor(unaryTerm.left)

    override def visitBinaryTerm(binaryTerm: TypedTree.BinaryTerm): Unit = {
      visitFactor(binaryTerm.left)
      visitExpression(binaryTerm.right)
      binaryTerm.operator match
        case Term.Operator.MULTIPLY => methodVisitor.visitInsn(IMUL)
        case Term.Operator.DIVIDE   => methodVisitor.visitInsn(IDIV)
    }

    override def visitIntLiteral(intLiteral: TypedTree.IntLiteral): Unit =
      methodVisitor.visitLdcInsn(intLiteral.literal)

    override def visitParenthesizedExpression(
        parenthesizedExpression: TypedTree.ParenthesizedExpression
    ): Unit = visitExpression(parenthesizedExpression.expression)

    override def visitPrintExpression(
        printExpression: TypedTree.PrintExpression
    ): Unit = {
      methodVisitor.visitFieldInsn(
        GETSTATIC,
        "java/lang/System",
        "out",
        "Ljava/io/PrintStream;"
      )
      visitExpression(printExpression.expression)
      val descriptor =
        s"(${TypeMapper.mapTypeToBytecodeString(printExpression.expression.`type`)})V"
      methodVisitor.visitMethodInsn(
        INVOKEVIRTUAL,
        "java/io/PrintStream",
        "println",
        descriptor,
        false
      )
    }

    override def visitFunctionApplication(
        functionApplication: TypedTree.FunctionApplication
    ): Unit = {
      functionApplication.arguments.foreach(visitExpression)
      val descriptor = TypeMapper.mapTypeAsFunctionTypeToBytecodeString(
        functionApplication.`type`
      )
      methodVisitor.visitMethodInsn(
        INVOKESTATIC,
        "com/github/tammo/Main", // TODO replace with dynamic owner
        functionApplication.identifier.name,
        descriptor,
        false
      )
    }

  }

}
