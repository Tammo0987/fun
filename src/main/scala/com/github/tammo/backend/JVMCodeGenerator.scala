package com.github.tammo.backend

import com.github.tammo.backend.CodeGenerationAction.*
import com.github.tammo.diagnostics.CompilerError
import com.github.tammo.diagnostics.CompilerError.CodeGenerationError
import com.github.tammo.frontend.`type`.TypedTree.*
import com.github.tammo.frontend.`type`.{Type, TypedTree}
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.{ClassWriter, MethodVisitor}

import scala.annotation.tailrec

object JVMCodeGenerator extends CodeGenerator {

  override def generate(
      compilationUnit: CompilationUnit
  ): Either[Seq[CodeGenerationError], Array[Byte]] = {
    val (assembledState, _) =
      generateCodeActions(compilationUnit).run(CodeGenState(Seq.empty, 1))
    val classWriter = new ClassWriter(
      ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
    )

    CodeGenerationInterpreter
      .interpret(assembledState.actions, classWriter)
      .map(_ => classWriter.toByteArray)
  }

  private def generateCodeActions(
      typedTree: TypedTree
  ): State[CodeGenState, Unit] = typedTree match
    case unit: CompilationUnit =>
      generateClassDeclaration(unit.fullyQualifiedName, unit.classDeclaration)
    case classDeclaration: ClassDeclaration =>
      generateClassDeclaration(classDeclaration.name, classDeclaration)
    case declaration: EffectDeclaration =>
      generateEffectDeclaration(declaration)
    case declaration: FunctionDeclaration =>
      generateFunctionDeclaration(declaration)
    case expression: Expression  => generateExpression(expression)
    case _: Parameter            => State.pure(())
    case _: NamespaceDeclaration => State.pure(())
    case _: TypedIdentifier      => State.pure(())
    case NotImplemented          => State.pure(())

  private def generateAll(
      typedTreeNodes: Iterable[TypedTree]
  ): State[CodeGenState, Unit] = {
    typedTreeNodes.foldLeft(
      State.pure[CodeGenState, Unit](
        Seq.empty[CodeGenerationAction]
      )
    ) { (acc, typedTreeNode) =>
      acc.flatMap(_ => generateCodeActions(typedTreeNode))
    }
  }

  private def generateClassDeclaration(
      fullyQualifiedClassName: String,
      classDeclaration: ClassDeclaration
  ): State[CodeGenState, Unit] =
    for {
      _ <- State.addAction(
        ClassAction(classWriter =>
          classWriter.visit(
            V17,
            ACC_PUBLIC,
            fullyQualifiedClassName,
            null,
            "java/lang/Object",
            new Array[String](0)
          )
        )
      )
      _ <- generateAll(classDeclaration.effects)
      _ <- generateAll(classDeclaration.functions)
      _ <- State.addAction(ClassAction(classWriter => classWriter.visitEnd()))
    } yield ()

  private def generateEffectDeclaration(
      effect: EffectDeclaration
  ): State[CodeGenState, Unit] = for {
    _ <- State.addAction(
      BeginMethod(
        ACC_PUBLIC | ACC_STATIC,
        effect.identifier.name,
        TypeMapper.mapTypeAsFunctionTypeToBytecodeString(
          effect.identifier.`type`
        )
      )
    )
    _ <- generateCodeActions(effect.body)
    _ <- State.addAction(MethodAction(methodVisitor => {
      generateReturn(effect.identifier.`type`, methodVisitor)
    }))
    _ <- State.resetLocalIndex()
    _ <- State.addAction(EndMethod)
  } yield ()

  private def generateFunctionDeclaration(
      function: FunctionDeclaration
  ): State[CodeGenState, Unit] = for {
    _ <- State.addAction(
      BeginMethod(
        ACC_PUBLIC | ACC_STATIC,
        function.identifier.name,
        TypeMapper.mapTypeAsFunctionTypeToBytecodeString(
          function.identifier.`type`
        )
      )
    )
    _ <- generateCodeActions(function.body)
    _ <- State.addAction(MethodAction(methodVisitor => {
      generateReturn(function.identifier.`type`, methodVisitor)
    }))
    _ <- State.resetLocalIndex()
    _ <- State.addAction(EndMethod)
  } yield ()

  private def generateExpression(
      expression: Expression
  ): State[CodeGenState, Unit] = expression match
    case expression: ArithmeticExpression =>
      generateArithmeticExpression(expression)
    case term: Term                  => generateTerm(term)
    case factor: Factor              => generateFactor(factor)
    case expression: PrintExpression => generatePrintExpression(expression)
    case application: FunctionApplication =>
      generateFunctionApplication(application)
    case StringLiteral(value, _) =>
      for {
        _ <- State.addAction(MethodAction(methodVisitor => {
          methodVisitor.visitLdcInsn(value)
        }))
      } yield ()
    case ParenthesizedExpression(expression, _) =>
      for {
        _ <- generateCodeActions(expression)
        index <- State.allocateLocalIndex()
        _ <- State.addAction(MethodAction(methodVisitor => {
          methodVisitor.visitVarInsn(
            mapStoreType(expression.`type`),
            index
          )
        }))
        _ <- State.addAction(
          MethodAction(_.visitVarInsn(mapLoadType(expression.`type`), 1))
        )
      } yield ()

  private def generatePrintExpression(
      printExpression: PrintExpression
  ): State[CodeGenState, Unit] = for {
    _ <- State.addAction(MethodAction(methodVisitor => {
      methodVisitor.visitFieldInsn(
        GETSTATIC,
        "java/lang/System",
        "out",
        "Ljava/io/PrintStream;"
      )
    }))
    _ <- generateCodeActions(printExpression.expression)
    _ <- State.addAction(MethodAction(methodVisitor => {
      val descriptor =
        s"(${TypeMapper.mapTypeToBytecodeString(printExpression.expression.`type`)})V"
      methodVisitor.visitMethodInsn(
        INVOKEVIRTUAL,
        "java/io/PrintStream",
        "println",
        descriptor,
        false
      )
    }))
  } yield ()

  private def generateFunctionApplication(
      functionApplication: FunctionApplication
  ): State[CodeGenState, Unit] = for {
    _ <- generateAll(functionApplication.arguments)
    _ <- State.addAction(MethodAction(methodVisitor => {
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
    }))
  } yield ()

  private def generateArithmeticExpression(
      expression: ArithmeticExpression
  ): State[CodeGenState, Unit] = expression match
    case operand: Operand => generateOperand(operand)
    case expression: BinaryArithmeticExpression =>
      generateBinaryArithmeticExpression(expression)

  private def generateOperand(
      operand: Operand
  ): State[CodeGenState, Unit] = for {
    _ <- generateCodeActions(operand.left)
  } yield ()

  private def generateBinaryArithmeticExpression(
      expression: BinaryArithmeticExpression
  ): State[CodeGenState, Unit] = for {
    _ <- generateCodeActions(expression.left)
    _ <- generateCodeActions(expression.right)
    _ <- State.addAction(MethodAction(methodVisitor => {
      expression.operator match
        case ArithmeticExpression.Operator.ADD =>
          methodVisitor.visitInsn(IADD)
        case ArithmeticExpression.Operator.SUBTRACT =>
          methodVisitor.visitInsn(ISUB)
    }))
  } yield ()

  private def generateTerm(term: Term): State[CodeGenState, Unit] =
    term match
      case term: UnaryTerm  => generateUnaryTerm(term)
      case term: BinaryTerm => generateBinaryTerm(term)

  private def generateUnaryTerm(
      term: UnaryTerm
  ): State[CodeGenState, Unit] = generateCodeActions(term.left)

  private def generateBinaryTerm(
      term: BinaryTerm
  ): State[CodeGenState, Unit] = for {
    _ <- generateCodeActions(term.left)
    _ <- generateCodeActions(term.right)
    _ <- State.addAction(MethodAction(methodVisitor => {
      term.operator match
        case Term.Operator.MULTIPLY =>
          methodVisitor.visitInsn(IMUL)
        case Term.Operator.DIVIDE =>
          methodVisitor.visitInsn(IDIV)
    }))
  } yield ()

  private def generateFactor(
      factor: Factor
  ): State[CodeGenState, Unit] = factor match
    case literal: IntLiteral =>
      State.addAction(
        MethodAction(methodVisitor =>
          methodVisitor.visitLdcInsn(literal.literal)
        )
      )

  @tailrec
  private def generateReturn(
      returnType: Type,
      methodVisitor: MethodVisitor
  ): Unit = {
    returnType match
      case Type.Unit        => methodVisitor.visitInsn(RETURN)
      case Type.Int         => methodVisitor.visitInsn(IRETURN)
      case Type.Boolean     => methodVisitor.visitInsn(IRETURN)
      case Type.String      => methodVisitor.visitInsn(ARETURN)
      case Type.StringArray => methodVisitor.visitInsn(ARETURN)
      case Type.FunctionType(_, returnType) =>
        generateReturn(returnType, methodVisitor)
      case Type.Variable() =>
        throw new IllegalStateException("Unexpected type variable")
  }

  @tailrec
  private def mapStoreType(`type`: Type): Int = `type` match
    case Type.Unit        => ISTORE
    case Type.Int         => ISTORE
    case Type.Boolean     => ISTORE
    case Type.String      => ASTORE
    case Type.StringArray => ASTORE
    case Type.FunctionType(_, returnType) =>
      mapStoreType(returnType)
    case Type.Variable() =>
      throw new IllegalStateException("Unexpected type variable")

  @tailrec
  private def mapLoadType(`type`: Type): Int = `type` match
    case Type.Unit        => ILOAD
    case Type.Int         => ILOAD
    case Type.Boolean     => ILOAD
    case Type.String      => ALOAD
    case Type.StringArray => ALOAD
    case Type.FunctionType(_, returnType) =>
      mapLoadType(returnType)
    case Type.Variable() =>
      throw new IllegalStateException("Unexpected type variable")

  private case class CodeGenState(
      actions: Seq[CodeGenerationAction],
      nextLocalIndex: Int
  )

  private case class State[S, A](
      run: S => (S, A)
  ) {

    def map[B](f: A => B): State[S, B] = State { state =>
      val (newState, oldValue) = run(state)
      (newState, f(oldValue))
    }

    def flatMap[B](f: A => State[S, B]): State[S, B] = State { state =>
      val (newState, oldValue) = run(state)
      f(oldValue).run(newState)
    }

  }

  private object State {

    def pure[S, A](x: A): State[S, A] = State(s => (s, x))

    def addAction(
        action: CodeGenerationAction
    ): State[CodeGenState, Unit] = State { state =>
      (state.copy(actions = state.actions :+ action), ())
    }

    def allocateLocalIndex(): State[CodeGenState, Int] = State { s =>
      val currentIndex = s.nextLocalIndex
      (s.copy(nextLocalIndex = currentIndex + 1), currentIndex)
    }

    def resetLocalIndex(): State[CodeGenState, Int] = State { s =>
      (s.copy(nextLocalIndex = 1), 1)
    }

  }

}
