package com.github.tammo.frontend.parse

import com.github.tammo.diagnostics.{PositionSpan, SourceFile}
import com.github.tammo.frontend.ast.SyntaxTree
import com.github.tammo.frontend.ast.SyntaxTree.*
import com.github.tammo.{FunBaseVisitor, FunParser}
import org.antlr.v4.runtime.{CommonToken, ParserRuleContext}

import scala.jdk.CollectionConverters.*

class SyntaxTreeBuilder(sourceFile: SourceFile)
    extends FunBaseVisitor[SyntaxTree] {

  override def visitCompilationUnit(
      ctx: FunParser.CompilationUnitContext
  ): SyntaxTree = {
    val namespaceDeclaration = Option(ctx.namespaceDeclaration())
      .map(_.qualifierIdentifier().getText)
      .map(NamespaceDeclaration(_, getPositionSpan(ctx.namespaceDeclaration())))

    val useDeclarations = ctx
      .useDeclaration()
      .asScala
      .map(visitUseDeclaration)
      .map(_.asInstanceOf[UseDeclaration])
      .toSeq

    val exposeDeclarations = ctx
      .exposeDeclaration()
      .asScala
      .map(visitExposeDeclaration)
      .map(_.asInstanceOf[ExposeDeclaration])
      .toSeq

    CompilationUnit(
      namespaceDeclaration,
      useDeclarations,
      exposeDeclarations,
      visitClassDeclaration(ctx.classDeclaration())
        .asInstanceOf[ClassDeclaration],
      getPositionSpan(ctx)
    )
  }

  override def visitNamespaceDeclaration(
      ctx: FunParser.NamespaceDeclarationContext
  ): SyntaxTree = NamespaceDeclaration(
    ctx.qualifierIdentifier().getText,
    getPositionSpan(ctx)
  )

  private def getPositionSpan(
      parserRuleContext: ParserRuleContext
  ): PositionSpan = {
    val startToken = parserRuleContext.getStart.asInstanceOf[CommonToken]
    val startOffset = startToken.getStartIndex
    val endOffset = startToken.getStopIndex
    PositionSpan(sourceFile.id, startOffset, endOffset)
  }

  override def visitUseDeclaration(
      ctx: FunParser.UseDeclarationContext
  ): SyntaxTree =
    UseDeclaration(
      ctx.qualifierIdentifier().Id().asScala.map(_.getText).toSeq,
      getPositionSpan(ctx)
    )

  override def visitExposeDeclaration(
      ctx: FunParser.ExposeDeclarationContext
  ): SyntaxTree =
    ExposeIdentifiers(Seq.empty, getPositionSpan(ctx))

  override def visitClassDeclaration(
      ctx: FunParser.ClassDeclarationContext
  ): SyntaxTree = {
    val name = ctx.Id().getText
    val parameters = readParameterList(ctx.parameterList())
    val effects = ctx
      .effectDeclaration()
      .asScala
      .map(visitEffectDeclaration)
      .map(_.asInstanceOf[EffectDeclaration])
      .toSeq
    val functions = ctx
      .functionDeclaration()
      .asScala
      .map(visitFunctionDeclaration)
      .map(_.asInstanceOf[FunctionDeclaration])
      .toSeq

    ClassDeclaration(
      name,
      parameters,
      effects,
      functions,
      getPositionSpan(ctx)
    )
  }

  override def visitEffectDeclaration(
      ctx: FunParser.EffectDeclarationContext
  ): SyntaxTree = {
    val name = ctx.Id().getText
    val parameter = readParameterList(ctx.parameterList())
    val returnType = Option(ctx.simpleType()).map(_.getText)
    val body = visitExpression(ctx.expression()).asInstanceOf[Expression]

    val startOffset = ctx.getStart.getStartIndex
    val endOffset = Option(ctx.simpleType())
      .map(_.getStop.getStopIndex)
      .getOrElse(ctx.parameterList().getStop.getStopIndex)

    EffectDeclaration(
      name,
      parameter,
      returnType,
      body,
      PositionSpan(sourceFile.id, startOffset, endOffset)
    )
  }

  override def visitFunctionDeclaration(
      ctx: FunParser.FunctionDeclarationContext
  ): SyntaxTree = {
    val name = ctx.Id().getText
    val parameter = readParameterList(ctx.parameterList())
    val returnType = Option(ctx.simpleType()).map(_.getText)
    val body = visitExpression(ctx.expression()).asInstanceOf[Expression]

    val startOffset = ctx.getStart.getStartIndex
    val endOffset = Option(ctx.simpleType())
      .map(_.getStop.getStopIndex)
      .getOrElse(ctx.parameterList().getStop.getStopIndex)

    FunctionDeclaration(
      name,
      parameter,
      returnType,
      body,
      PositionSpan(sourceFile.id, startOffset, endOffset)
    )
  }

  // TODO more work
  override def visitExpression(ctx: FunParser.ExpressionContext): SyntaxTree = {
    if (ctx.printExpression() != null) {
      visitPrintExpression(ctx.printExpression())
    } else if (ctx.simpleExpression() != null) {
      visitSimpleExpression(ctx.simpleExpression())
    } else if (ctx.expression() != null) {
      ParenthesizedExpression(
        visitExpression(ctx.expression()).asInstanceOf[Expression],
        getPositionSpan(ctx)
      )
    } else if (ctx.String() != null) {
      StringLiteral(
        ctx.String().getText.substring(1, ctx.String().getText.length - 1),
        getPositionSpan(ctx)
      )
    } else {
      super.visitExpression(ctx)
    }
  }

  override def visitPrintExpression(
      ctx: FunParser.PrintExpressionContext
  ): SyntaxTree = {
    PrintExpression(
      visitExpression(ctx.expression()).asInstanceOf[Expression],
      getPositionSpan(ctx)
    )
  }

  override def visitFunctionApplication(
      ctx: FunParser.FunctionApplicationContext
  ): SyntaxTree = {
    val arguments = ctx
      .expression()
      .asScala
      .map(visitExpression)
      .map(_.asInstanceOf[Expression])
      .toSeq
    FunctionApplication(ctx.Id().getText, arguments, getPositionSpan(ctx))
  }

  override def visitSimpleExpression(
      ctx: FunParser.SimpleExpressionContext
  ): SyntaxTree = {
    if (ctx.expression(0) != null) {
      val operation = ctx.operand.getText match
        case "+" => ArithmeticExpression.Operator.ADD
        case "-" => ArithmeticExpression.Operator.SUBTRACT
        case _ =>
          throw new IllegalStateException(
            s"Unexpected operator ${ctx.operand.getText}"
          )

      BinaryArithmeticExpression(
        visitTerm(ctx.term()).asInstanceOf[Term],
        visitExpression(ctx.expression().getFirst).asInstanceOf[Expression],
        operation,
        getPositionSpan(ctx)
      )
    } else {
      Operand(visitTerm(ctx.term()).asInstanceOf[Term], getPositionSpan(ctx))
    }
  }

  override def visitTerm(ctx: FunParser.TermContext): SyntaxTree = {
    if (ctx.expression(0) != null) {
      val operation = ctx.operand.getText match
        case "*" => Term.Operator.MULTIPLY
        case "/" => Term.Operator.DIVIDE
        case _ =>
          throw new IllegalStateException(
            s"Unexpected operator ${ctx.operand.getText}"
          )

      BinaryTerm(
        visitFactor(ctx.factor()).asInstanceOf[Factor],
        visitExpression(ctx.expression().getFirst).asInstanceOf[Expression],
        operation,
        getPositionSpan(ctx)
      )
    } else {
      UnaryTerm(
        visitFactor(ctx.factor()).asInstanceOf[Factor],
        getPositionSpan(ctx)
      )
    }
  }

  override def visitFactor(ctx: FunParser.FactorContext): SyntaxTree = {
    if (ctx.Number() != null) {
      IntLiteral(ctx.Number().getText.toInt, getPositionSpan(ctx))
    } else {
      super.visitFactor(ctx)
    }
  }

  private def readParameterList(
      ctx: FunParser.ParameterListContext
  ): Seq[Parameter] = {
    Seq
      .range(0, ctx.Id().size())
      .map(i => {
        val startOffset = ctx.Id(i).getSymbol.getStartIndex
        val endOffset = ctx.simpleType(i).getStart.getStopIndex
        val positionSpan = PositionSpan(sourceFile.id, startOffset, endOffset)
        Parameter(
          ctx.Id(i).getText,
          ctx.simpleType(i).getText,
          positionSpan
        )
      })
  }

}
