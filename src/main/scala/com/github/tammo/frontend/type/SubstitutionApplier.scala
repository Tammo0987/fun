package com.github.tammo.frontend.`type`

import com.github.tammo.frontend.`type`.TypedTree.*

object SubstitutionApplier {

  def applySubstitutions(
      typedTree: TypedTree,
      apply: Type => Type
  ): TypedTree = typedTree match
    case compilationUnit: CompilationUnit =>
      applySubstitutionToCompilationUnit(compilationUnit, apply)
    case namespaceDeclaration: NamespaceDeclaration =>
      namespaceDeclaration
    case classDeclaration: ClassDeclaration =>
      applySubstitutionToClassDeclaration(classDeclaration, apply)
    case declaration: EffectDeclaration =>
      applySubstitutionToEffectDeclaration(declaration, apply)
    case declaration: FunctionDeclaration =>
      applySubstitutionToFunctionDeclaration(declaration, apply)
    case parameter: Parameter => applySubstitutionToParameter(parameter, apply)
    case identifier: TypedIdentifier =>
      applySubstitutionToTypedIdentifier(identifier, apply)
    case NotImplemented => NotImplemented
    case expression: Expression =>
      applySubstitutionToExpression(expression, apply)

  private def applySubstitutionToCompilationUnit(
      compilationUnit: CompilationUnit,
      apply: Type => Type
  ): CompilationUnit = CompilationUnit(
    compilationUnit.namespace,
    applySubstitutionToClassDeclaration(
      compilationUnit.classDeclaration,
      apply
    ),
    compilationUnit.span
  )

  private def applySubstitutionToClassDeclaration(
      classDeclaration: ClassDeclaration,
      apply: Type => Type
  ): ClassDeclaration = ClassDeclaration(
    classDeclaration.name,
    classDeclaration.effects.map(
      applySubstitutionToEffectDeclaration(_, apply)
    ),
    classDeclaration.functions.map(
      applySubstitutionToFunctionDeclaration(_, apply)
    ),
    classDeclaration.span
  )

  private def applySubstitutionToEffectDeclaration(
      effectDeclaration: EffectDeclaration,
      apply: Type => Type
  ): EffectDeclaration = EffectDeclaration(
    applySubstitutionToTypedIdentifier(effectDeclaration.identifier, apply),
    effectDeclaration.parameters.map(applySubstitutionToParameter(_, apply)),
    applySubstitutionToExpression(effectDeclaration.body, apply),
    effectDeclaration.span
  )

  private def applySubstitutionToFunctionDeclaration(
      functionDeclaration: FunctionDeclaration,
      apply: Type => Type
  ): FunctionDeclaration = FunctionDeclaration(
    applySubstitutionToTypedIdentifier(functionDeclaration.identifier, apply),
    functionDeclaration.parameters.map(applySubstitutionToParameter(_, apply)),
    applySubstitutionToExpression(functionDeclaration.body, apply),
    functionDeclaration.span
  )

  private def applySubstitutionToTypedIdentifier(
      typedIdentifier: TypedIdentifier,
      apply: Type => Type
  ): TypedIdentifier = TypedIdentifier(
    typedIdentifier.name,
    apply(typedIdentifier.`type`),
    typedIdentifier.span
  )

  private def applySubstitutionToParameter(
      parameter: Parameter,
      apply: Type => Type
  ): Parameter = Parameter(
    applySubstitutionToTypedIdentifier(parameter.identifier, apply),
    parameter.span
  )

  private def applySubstitutionToExpression(
      expression: Expression,
      apply: Type => Type
  ): Expression = expression match
    case PrintExpression(expression, span) =>
      PrintExpression(applySubstitutionToExpression(expression, apply), span)
    case ParenthesizedExpression(expression, span) =>
      ParenthesizedExpression(
        applySubstitutionToExpression(expression, apply),
        span
      )
    case literal: StringLiteral => literal
    case application: FunctionApplication =>
      applySubstitutionToFunctionApplication(application, apply)
    case expression: ArithmeticExpression =>
      applySubstitutionToArithmeticExpression(expression, apply)
    case term: Term     => applySubstitutionToTerm(term, apply)
    case factor: Factor => applySubstitutionToFactor(factor)

  private def applySubstitutionToFunctionApplication(
      functionApplication: FunctionApplication,
      apply: Type => Type
  ): FunctionApplication = FunctionApplication(
    applySubstitutionToTypedIdentifier(functionApplication.identifier, apply),
    functionApplication.arguments.map(applySubstitutionToExpression(_, apply)),
    functionApplication.span
  )

  private def applySubstitutionToArithmeticExpression(
      expression: ArithmeticExpression,
      apply: Type => Type
  ): ArithmeticExpression = expression match
    case operand: Operand =>
      Operand(
        apply(operand.`type`),
        applySubstitutionToTerm(operand.left, apply),
        operand.span
      )
    case expression: BinaryArithmeticExpression =>
      BinaryArithmeticExpression(
        apply(expression.`type`),
        applySubstitutionToTerm(expression.left, apply),
        applySubstitutionToExpression(expression.right, apply),
        expression.operator,
        expression.span
      )

  private def applySubstitutionToTerm(
      term: Term,
      apply: Type => Type
  ): Term = term match
    case term: UnaryTerm =>
      UnaryTerm(
        apply(term.`type`),
        applySubstitutionToFactor(term.left),
        term.span
      )
    case term: BinaryTerm =>
      BinaryTerm(
        apply(term.`type`),
        applySubstitutionToFactor(term.left),
        applySubstitutionToExpression(term.right, apply),
        term.operator,
        term.span
      )

  private def applySubstitutionToFactor(factor: Factor): Factor = factor match
    case literal: IntLiteral => literal

}
