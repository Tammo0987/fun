package com.github.tammo.frontend.`type`

import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.`type`.TypedTree.TypedIdentifier
import SyntaxTree.{ArithmeticExpression, Term}

object TypeAnnotate {

  def annotateTypes(
      syntaxTree: SyntaxTree.CompilationUnit
  ): TypedTree.CompilationUnit = {
    val namespaceDeclaration = syntaxTree.namespace
      .map(_.identifier)
      .map(TypedTree.NamespaceDeclaration.apply)

    TypedTree.CompilationUnit(
      namespaceDeclaration,
      annotateTypeAtClassDeclaration(syntaxTree.classDeclaration)
    )
  }

  private def annotateTypeAtClassDeclaration(
      classDeclaration: SyntaxTree.ClassDeclaration
  ): TypedTree.ClassDeclaration =
    TypedTree.ClassDeclaration(
      classDeclaration.name,
      classDeclaration.effects.map(annotateTypesAtEffectDeclaration),
      classDeclaration.functions.map(annotateTypesAtFunctionDeclaration)
    )

  private def annotateTypesAtEffectDeclaration(
      effectDeclaration: SyntaxTree.EffectDeclaration
  ): TypedTree.EffectDeclaration = {
    val typedIdentifier = TypedTree.TypedIdentifier(
      effectDeclaration.name,
      createFunctionType(
        effectDeclaration.returnType,
        effectDeclaration.parameters
      )
    )

    TypedTree.EffectDeclaration(
      typedIdentifier,
      effectDeclaration.parameters.map(annotateTypeAtParameter),
      annotateTypeAtExpression(effectDeclaration.body)
    )
  }

  private def annotateTypesAtFunctionDeclaration(
      functionDeclaration: SyntaxTree.FunctionDeclaration
  ): TypedTree.FunctionDeclaration = {
    val typedIdentifier = TypedTree.TypedIdentifier(
      functionDeclaration.name,
      createFunctionType(
        functionDeclaration.returnType,
        functionDeclaration.parameters
      )
    )

    TypedTree.FunctionDeclaration(
      typedIdentifier,
      functionDeclaration.parameters.map(annotateTypeAtParameter),
      annotateTypeAtExpression(functionDeclaration.body)
    )
  }

  private def annotateTypeAtParameter(
      parameter: SyntaxTree.Parameter
  ): TypedTree.Parameter = TypedTree.Parameter(
    TypedTree.TypedIdentifier(
      parameter.identifier,
      Type.fromString(parameter.`type`)
    )
  )

  private def annotateTypeAtExpression(
      expression: SyntaxTree.Expression
  ): TypedTree.Expression = expression match
    case expression: SyntaxTree.ArithmeticExpression =>
      annotateTypeAtArithmeticExpression(expression)
    case term: SyntaxTree.Term     => annotateTypeAtTerm(term)
    case factor: SyntaxTree.Factor => annotateTypeAtFactor(factor)
    case application: SyntaxTree.FunctionApplication =>
      annotateTypeAtFunctionApplication(application)
    case SyntaxTree.StringLiteral(value) => TypedTree.StringLiteral(value)
    case SyntaxTree.PrintExpression(expression) =>
      TypedTree.PrintExpression(annotateTypeAtExpression(expression))
    case SyntaxTree.ParenthesizedExpression(expression) =>
      TypedTree.ParenthesizedExpression(annotateTypeAtExpression(expression))

  private def annotateTypeAtArithmeticExpression(
      arithmeticExpression: SyntaxTree.ArithmeticExpression
  ): TypedTree.ArithmeticExpression = arithmeticExpression match
    case SyntaxTree.Operand(left) =>
      TypedTree.Operand(Type.Variable(), annotateTypeAtTerm(left))
    case SyntaxTree.BinaryArithmeticExpression(left, right, operator) =>
      val mappedOperator = operator match
        case SyntaxTree.ArithmeticExpression.Operator.ADD =>
          TypedTree.ArithmeticExpression.Operator.ADD
        case SyntaxTree.ArithmeticExpression.Operator.SUBTRACT =>
          TypedTree.ArithmeticExpression.Operator.SUBTRACT
      TypedTree.BinaryArithmeticExpression(
        Type.Variable(),
        annotateTypeAtTerm(left),
        annotateTypeAtExpression(right),
        mappedOperator
      )

  private def annotateTypeAtTerm(term: SyntaxTree.Term): TypedTree.Term =
    term match
      case SyntaxTree.UnaryTerm(left) =>
        TypedTree.UnaryTerm(Type.Variable(), annotateTypeAtFactor(left))
      case SyntaxTree.BinaryTerm(left, right, operator) =>
        val mappedOperator = operator match
          case SyntaxTree.Term.Operator.MULTIPLY =>
            TypedTree.Term.Operator.MULTIPLY
          case SyntaxTree.Term.Operator.DIVIDE => TypedTree.Term.Operator.DIVIDE

        TypedTree.BinaryTerm(
          Type.Variable(),
          annotateTypeAtFactor(left),
          annotateTypeAtExpression(right),
          mappedOperator
        )

  private def annotateTypeAtFactor(
      factor: SyntaxTree.Factor
  ): TypedTree.Factor = factor match
    case SyntaxTree.IntLiteral(literal) => TypedTree.IntLiteral(literal)

  private def annotateTypeAtFunctionApplication(
      functionApplication: SyntaxTree.FunctionApplication
  ): TypedTree.FunctionApplication =
    TypedTree.FunctionApplication(
      TypedIdentifier(functionApplication.identifier, Type.Variable()),
      functionApplication.arguments.map(annotateTypeAtExpression)
    )

  private def createFunctionType(
      returnType: String,
      parameters: Seq[SyntaxTree.Parameter]
  ): Type = parameters.foldRight(Type.fromString(returnType)) { (arg, acc) =>
    Type.FunctionType(Type.fromString(arg.`type`), acc)
  }

}
