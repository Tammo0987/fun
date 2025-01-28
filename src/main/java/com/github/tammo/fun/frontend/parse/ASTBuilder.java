package com.github.tammo.fun.frontend.parse;

import com.github.tammo.FunBaseVisitor;
import com.github.tammo.FunParser;
import com.github.tammo.fun.frontend.ast.Expression;
import com.github.tammo.fun.frontend.ast.SyntaxNode;
import com.github.tammo.fun.frontend.ast.SyntaxNode.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ASTBuilder extends FunBaseVisitor<SyntaxNode> {

    @Override
    public SyntaxNode visitCompilationUnit(FunParser.CompilationUnitContext ctx) {
        Optional<NamespaceDeclaration> namespaceDeclaration = Optional.ofNullable(ctx.namespaceDeclaration())
                .map(FunParser.NamespaceDeclarationContext::qualifierIdentifier)
                .map(FunParser.QualifierIdentifierContext::getText)
                .map(NamespaceDeclaration::new);

        List<UseDeclaration> useDeclarations = ctx.useDeclaration().stream()
                .map(this::visitUseDeclaration)
                .map(UseDeclaration.class::cast)
                .toList();

        List<ExposeDeclaration> exposeDeclarations = ctx.exposeDeclaration().stream()
                .map(this::visitExposeDeclaration)
                .map(ExposeDeclaration.class::cast)
                .toList();

        return new CompilationUnit(
                namespaceDeclaration,
                useDeclarations,
                exposeDeclarations,
                (ClassDeclaration) visitClassDeclaration(ctx.classDeclaration())
        );
    }

    @Override
    public SyntaxNode visitNamespaceDeclaration(FunParser.NamespaceDeclarationContext ctx) {
        String qualifierIdentifier = ctx.qualifierIdentifier().getText();
        return new NamespaceDeclaration(qualifierIdentifier);
    }

    @Override
    public SyntaxNode visitUseDeclaration(FunParser.UseDeclarationContext ctx) {
        Set<String> qualifierIdentifiers = ctx.qualifierIdentifier().Id().stream()
                .map(ParseTree::getText)
                .collect(Collectors.toSet());
        return new UseDeclaration(qualifierIdentifiers);
    }

    @Override
    public SyntaxNode visitExposeDeclaration(FunParser.ExposeDeclarationContext ctx) {
        return new ExposeIdentifiers(Set.of());
    }

    @Override
    public SyntaxNode visitClassDeclaration(FunParser.ClassDeclarationContext ctx) {
        String name = ctx.Id().getText();
        ParameterList parameters = (ParameterList) visitParameterList(ctx.parameterList());
        List<EffectDeclaration> effectDeclarations = ctx
                .effectDeclaration()
                .stream()
                .map(this::visitEffectDeclaration)
                .map(EffectDeclaration.class::cast)
                .toList();
        List<FunctionDeclaration> functionDeclarations = ctx
                .functionDeclaration()
                .stream()
                .map(this::visitFunctionDeclaration)
                .map(FunctionDeclaration.class::cast)
                .toList();
        return new ClassDeclaration(name, parameters, effectDeclarations, functionDeclarations);
    }

    @Override
    public SyntaxNode visitParameterList(FunParser.ParameterListContext ctx) {
        List<Parameter> parameters = new ArrayList<>();

        if (ctx.Id(0) != null && ctx.simpleType(0) != null) {
            parameters.add(new Parameter(ctx.Id(0).getText(), ctx.simpleType(0).getText()));
        }

        for (int i = 1; i < ctx.Id().size(); i++) {
            parameters.add(new Parameter(ctx.Id(i).getText(), ctx.simpleType(i).getText()));
        }

        return new ParameterList(parameters);
    }

    @Override
    public SyntaxNode visitEffectDeclaration(FunParser.EffectDeclarationContext ctx) {
        String name = ctx.Id().getText();
        ParameterList parameters = (ParameterList) visitParameterList(ctx.parameterList());
        String returnType = ctx.simpleType().getText();
        Expression body = (Expression) visitExpression(ctx.expression());
        return new EffectDeclaration(name, parameters, returnType, body);
    }

    @Override
    public SyntaxNode visitFunctionDeclaration(FunParser.FunctionDeclarationContext ctx) {
        final var name = ctx.Id().getText();
        final var parameterList = (ParameterList) visitParameterList(ctx.parameterList());
        final var returnType = ctx.simpleType().getText();
        final var expression = visitExpression(ctx.expression());
        return new FunctionDeclaration(name, parameterList, returnType, (Expression) expression);
    }

    @Override
    public SyntaxNode visitExpression(FunParser.ExpressionContext ctx) {
        if (ctx.printExpression() != null) {
            return visitPrintExpression(ctx.printExpression());
        }

        if (ctx.simpleExpression() != null) {
            return visitSimpleExpression(ctx.simpleExpression());
        }

        return super.visitExpression(ctx);
    }

    @Override
    public SyntaxNode visitPrintExpression(FunParser.PrintExpressionContext ctx) {
        if (ctx.String() != null) {
            return new Expression.PrintlnExpression(new Expression.StringLiteral(ctx.String().getText()));
        }

        if (ctx.functionCall() != null) {
            return new Expression.PrintlnExpression((Expression) visitFunctionCall(ctx.functionCall()));
        }

        return super.visitPrintExpression(ctx);
    }

    @Override
    public SyntaxNode visitFunctionCall(FunParser.FunctionCallContext ctx) {
        final var arguments = ctx.expression().stream().map(this::visitExpression).map(Expression.class::cast).toList();
        return new Expression.FunctionCall(ctx.Id().getText(), arguments);
    }

    @Override
    public SyntaxNode visitSimpleExpression(FunParser.SimpleExpressionContext ctx) {
        if (ctx.term().size() > 1) {
            Expression.ArithmeticExpression.Operation operation = switch (ctx.operand.getText()) {
                case "+" -> Expression.ArithmeticExpression.Operation.ADD;
                case "-" -> Expression.ArithmeticExpression.Operation.SUBTRACT;
                default -> throw new IllegalStateException("Unexpected value: " + ctx.operand.getText());
            };
            return new Expression.BinaryArithmeticExpression(
                    (Expression.Term) visitTerm(ctx.term(0)),
                    (Expression.Term) visitTerm(ctx.term(1)),
                    operation
            );
        } else {
            return new Expression.Operand((Expression.Term) visitTerm(ctx.term(0)));
        }
    }

    @Override
    public SyntaxNode visitTerm(FunParser.TermContext ctx) {
        if (ctx.factor().size() > 1) {
            final var operation = switch (ctx.operand.getText()) {
                case "*" -> Expression.Term.Operation.MULTIPLY;
                case "/" -> Expression.Term.Operation.DIVIDE;
                default -> throw new IllegalStateException("Unexpected value: " + ctx.operand.getText());
            };

            return new Expression.BinaryTerm(
                    (Expression.Factor) visitFactor(ctx.factor(0)),
                    (Expression.Factor) visitFactor(ctx.factor(1)),
                    operation
            );
        } else {
            return new Expression.SimpleTerm((Expression.Factor) visitFactor(ctx.factor(0)));
        }
    }

    @Override
    public SyntaxNode visitFactor(FunParser.FactorContext ctx) {
        if (ctx.Number() != null) {
            return new Expression.IntegerLiteral(Integer.parseInt(ctx.Number().getText()));
        }

        if (ctx.simpleExpression() != null) {
            return new Expression.ParenthesizedExpression((Expression.ArithmeticExpression) visitSimpleExpression(ctx.simpleExpression()));
        }

        return super.visitFactor(ctx);
    }
}

