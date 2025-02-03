package com.github.tammo.frontend.`type`

import com.github.tammo.diagnostics.PositionSpan
import com.github.tammo.frontend.`type`.TypedTree.TypedIdentifier
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.ast.SyntaxTree.{ArithmeticExpression, Term}

object TypeAnnotate {

  def annotateTypes(
      syntaxTree: SyntaxTree.CompilationUnit
  ): TypedTree.CompilationUnit = {
    val namespaceDeclaration = syntaxTree.namespace
      .map(namespace =>
        TypedTree.NamespaceDeclaration(namespace.identifier, namespace.span)
      )

    TypedTree.CompilationUnit(
      namespaceDeclaration,
      annotateTypeAtClassDeclaration(syntaxTree.classDeclaration),
      syntaxTree.span
    )
  }

  private def annotateTypeAtClassDeclaration(
      classDeclaration: SyntaxTree.ClassDeclaration
  ): TypedTree.ClassDeclaration =
    TypedTree.ClassDeclaration(
      classDeclaration.name,
      classDeclaration.effects.map(annotateTypesAtEffectDeclaration),
      classDeclaration.functions.map(annotateTypesAtFunctionDeclaration),
      classDeclaration.span
    )

  private def annotateTypesAtEffectDeclaration(
      effectDeclaration: SyntaxTree.EffectDeclaration
  ): TypedTree.EffectDeclaration = {
    val typedIdentifier = TypedTree.TypedIdentifier(
      effectDeclaration.name,
      createFunctionType(
        effectDeclaration.returnType,
        effectDeclaration.parameters
      ),
      PositionSpan(
        effectDeclaration.span.fileId,
        effectDeclaration.span.startOffset,
        effectDeclaration.span.startOffset + effectDeclaration.name.length
      )
    )

    TypedTree.EffectDeclaration(
      typedIdentifier,
      effectDeclaration.parameters.map(annotateTypeAtParameter),
      annotateTypeAtExpression(effectDeclaration.body),
      effectDeclaration.span
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
      ),
      PositionSpan(
        functionDeclaration.span.fileId,
        functionDeclaration.span.startOffset,
        functionDeclaration.span.startOffset + functionDeclaration.name.length
      )
    )

    TypedTree.FunctionDeclaration(
      typedIdentifier,
      functionDeclaration.parameters.map(annotateTypeAtParameter),
      annotateTypeAtExpression(functionDeclaration.body),
      functionDeclaration.span
    )
  }

  private def annotateTypeAtParameter(
      parameter: SyntaxTree.Parameter
  ): TypedTree.Parameter = TypedTree.Parameter(
    TypedTree.TypedIdentifier(
      parameter.identifier,
      Type.fromString(parameter.`type`),
      PositionSpan(
        parameter.span.fileId,
        parameter.span.startOffset,
        parameter.span.startOffset + parameter.identifier.length
      )
    ),
    parameter.span
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
    case SyntaxTree.StringLiteral(value, span) =>
      TypedTree.StringLiteral(value, span)
    case SyntaxTree.PrintExpression(expression, span) =>
      TypedTree.PrintExpression(annotateTypeAtExpression(expression), span)
    case SyntaxTree.ParenthesizedExpression(expression, span) =>
      TypedTree.ParenthesizedExpression(
        annotateTypeAtExpression(expression),
        span
      )

  private def annotateTypeAtArithmeticExpression(
      arithmeticExpression: SyntaxTree.ArithmeticExpression
  ): TypedTree.ArithmeticExpression = arithmeticExpression match
    case SyntaxTree.Operand(left, span) =>
      TypedTree.Operand(Type.Variable(), annotateTypeAtTerm(left), span)
    case SyntaxTree.BinaryArithmeticExpression(left, right, operator, span) =>
      val mappedOperator = operator match
        case SyntaxTree.ArithmeticExpression.Operator.ADD =>
          TypedTree.ArithmeticExpression.Operator.ADD
        case SyntaxTree.ArithmeticExpression.Operator.SUBTRACT =>
          TypedTree.ArithmeticExpression.Operator.SUBTRACT
      TypedTree.BinaryArithmeticExpression(
        Type.Variable(),
        annotateTypeAtTerm(left),
        annotateTypeAtExpression(right),
        mappedOperator,
        span
      )

  private def annotateTypeAtTerm(term: SyntaxTree.Term): TypedTree.Term =
    term match
      case SyntaxTree.UnaryTerm(left, span) =>
        TypedTree.UnaryTerm(Type.Variable(), annotateTypeAtFactor(left), span)
      case SyntaxTree.BinaryTerm(left, right, operator, span) =>
        val mappedOperator = operator match
          case SyntaxTree.Term.Operator.MULTIPLY =>
            TypedTree.Term.Operator.MULTIPLY
          case SyntaxTree.Term.Operator.DIVIDE => TypedTree.Term.Operator.DIVIDE

        TypedTree.BinaryTerm(
          Type.Variable(),
          annotateTypeAtFactor(left),
          annotateTypeAtExpression(right),
          mappedOperator,
          span
        )

  private def annotateTypeAtFactor(
      factor: SyntaxTree.Factor
  ): TypedTree.Factor = factor match
    case SyntaxTree.IntLiteral(literal, span) =>
      TypedTree.IntLiteral(literal, span)

  private def annotateTypeAtFunctionApplication(
      functionApplication: SyntaxTree.FunctionApplication
  ): TypedTree.FunctionApplication =
    TypedTree.FunctionApplication(
      TypedIdentifier(
        functionApplication.identifier,
        Type.Variable(),
        PositionSpan(
          functionApplication.span.fileId,
          functionApplication.span.startOffset,
          functionApplication.span.startOffset + functionApplication.identifier.length
        )
      ),
      functionApplication.arguments.map(annotateTypeAtExpression),
      functionApplication.span
    )

  private def createFunctionType(
      returnType: Option[String],
      parameters: Seq[SyntaxTree.Parameter]
  ): Type = {
    val mappedReturnType =
      returnType.map(Type.fromString).getOrElse(Type.Variable())
    parameters.foldRight(mappedReturnType) { (arg, acc) =>
      Type.FunctionType(Type.fromString(arg.`type`), acc)
    }
  }

}
