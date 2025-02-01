package com.github.tammo.frontend.`type`

import com.github.tammo.frontend.`type`.TypedTree.*

object TypedTreeVisitor {

  trait Visitor {
    def visitNotImplemented(
        notImplemented: TypedTree.NotImplemented.type
    ): Unit = {}

    def visitNamespaceDeclaration(namespace: NamespaceDeclaration): Unit = {}

    def visitCompilationUnit(compilationUnit: CompilationUnit): Unit = {}

    def visitClassDeclaration(classDeclaration: ClassDeclaration): Unit = {}

    def visitParameter(parameter: Parameter): Unit = {}

    def visitEffectDeclaration(effect: EffectDeclaration): Unit = {}

    def visitFunctionDeclaration(function: FunctionDeclaration): Unit = {}

    def visitExpression(expression: Expression): Unit = expression match
      case expression: PrintExpression => visitPrintExpression(expression)
      case literal: StringLiteral      => visitStringLiteral(literal)
      case application: FunctionApplication =>
        visitFunctionApplication(application)
      case expression: ArithmeticExpression =>
        visitArithmeticExpression(expression)
      case term: Term     => visitTerm(term)
      case factor: Factor => visitFactor(factor)
      case expression: ParenthesizedExpression =>
        visitParenthesizedExpression(expression)

    def visitArithmeticExpression(
        arithmeticExpression: ArithmeticExpression
    ): Unit = arithmeticExpression match
      case operand: Operand => visitOperand(operand)
      case expression: BinaryArithmeticExpression =>
        visitBinaryArithmeticExpression(expression)

    def visitTerm(term: Term): Unit = term match
      case term: UnaryTerm  => visitUnaryTerm(term)
      case term: BinaryTerm => visitBinaryTerm(term)

    def visitFactor(factor: Factor): Unit = factor match
      case literal: IntLiteral => visitIntLiteral(literal)

    def visitPrintExpression(printExpression: PrintExpression): Unit = {}

    def visitFunctionApplication(
        functionApplication: FunctionApplication
    ): Unit = {}

    def visitOperand(operand: Operand): Unit = {}

    def visitBinaryArithmeticExpression(
        binaryArithmeticExpression: BinaryArithmeticExpression
    ): Unit = {}

    def visitUnaryTerm(unaryTerm: UnaryTerm): Unit = {}

    def visitBinaryTerm(binaryTerm: BinaryTerm): Unit = {}

    def visitParenthesizedExpression(
        parenthesizedExpression: ParenthesizedExpression
    ): Unit = {}

    def visitStringLiteral(stringLiteral: StringLiteral): Unit = {}

    def visitIntLiteral(intLiteral: IntLiteral): Unit = {}

    def visitTypeIdentifier(typedIdentifier: TypedIdentifier): Unit = {}
  }

  def visit(node: TypedTree, visitor: Visitor): Unit = node match {
    case NotImplemented           => visitor.visitNotImplemented(NotImplemented)
    case nd: NamespaceDeclaration => visitor.visitNamespaceDeclaration(nd)
    case cu: CompilationUnit      => visitor.visitCompilationUnit(cu)
    case cd: ClassDeclaration     => visitor.visitClassDeclaration(cd)
    case p: Parameter             => visitor.visitParameter(p)
    case e: EffectDeclaration     => visitor.visitEffectDeclaration(e)
    case f: FunctionDeclaration   => visitor.visitFunctionDeclaration(f)
    case e: Expression            => visitor.visitExpression(e)
    case pe: PrintExpression      => visitor.visitPrintExpression(pe)
    case sl: StringLiteral        => visitor.visitStringLiteral(sl)
    case fa: FunctionApplication  => visitor.visitFunctionApplication(fa)
    case o: Operand               => visitor.visitOperand(o)
    case bae: BinaryArithmeticExpression =>
      visitor.visitBinaryArithmeticExpression(bae)
    case ut: UnaryTerm  => visitor.visitUnaryTerm(ut)
    case bt: BinaryTerm => visitor.visitBinaryTerm(bt)
    case il: IntLiteral => visitor.visitIntLiteral(il)
    case pex: ParenthesizedExpression =>
      visitor.visitParenthesizedExpression(pex)
    case ti: TypedIdentifier => visitor.visitTypeIdentifier(ti)
  }

}
