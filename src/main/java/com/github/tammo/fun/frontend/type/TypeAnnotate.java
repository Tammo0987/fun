package com.github.tammo.fun.frontend.type;

import com.github.tammo.fun.frontend.ast.Expression;
import com.github.tammo.fun.frontend.ast.SyntaxNode;

import java.util.List;

public class TypeAnnotate {

    public static TypedTreeNode annotateTypes(SyntaxNode syntaxNode) {
        return switch (syntaxNode) {
            case SyntaxNode.ClassDeclaration classDeclaration -> {
                List<TypedTreeNode.EffectDeclaration> effectDeclarations = classDeclaration.effectDeclarations()
                        .stream()
                        .map(TypeAnnotate::annotateTypes)
                        .map(TypedTreeNode.EffectDeclaration.class::cast)
                        .toList();
                yield new TypedTreeNode.ObjectDeclaration(
                        classDeclaration.name(),
                        effectDeclarations
                );
            }
            case SyntaxNode.CompilationUnit compilationUnit -> new TypedTreeNode.CompilationUnit(
                    ((TypedTreeNode.ObjectDeclaration) annotateTypes(compilationUnit.classDeclaration()))
            );
            case SyntaxNode.EffectDeclaration effectDeclaration -> {
                List<TypedTreeNode.Parameter> typedParameter = effectDeclaration.parameters().parameters().stream()
                        .map(TypeAnnotate::annotateTypes)
                        .map(TypedTreeNode.Parameter.class::cast)
                        .toList();
                TypedTreeNode.Expression typedBody = (TypedTreeNode.Expression) annotateTypes(effectDeclaration.body());
                yield new TypedTreeNode.EffectDeclaration(
                        new TypedTreeNode.TypedIdentifier(effectDeclaration.name(), new FunType.TypeVariable()),
                        typedParameter,
                        typedBody
                );
            }
            case SyntaxNode.ExposeDeclaration ignore -> new TypedTreeNode.NotImplemented();
            case SyntaxNode.NamespaceDeclaration namespaceDeclaration ->
                    new TypedTreeNode.NamespaceDeclaration(namespaceDeclaration.qualifierIdentifier());
            case SyntaxNode.UseDeclaration ignore -> new TypedTreeNode.NotImplemented();
            case SyntaxNode.Parameter parameter -> new TypedTreeNode.Parameter(
                    new TypedTreeNode.TypedIdentifier(parameter.name(), FunType.fromString(parameter.type()))
            );
            case SyntaxNode.ParameterList ignored -> new TypedTreeNode.NotImplemented();
            case SyntaxNode.FunctionDeclaration ignored -> new TypedTreeNode.NotImplemented();
            case Expression expression -> annotateTypesAtExpression(expression);
        };
    }

    private static TypedTreeNode.Expression annotateTypesAtExpression(Expression expression) {
        return switch (expression) {
            case Expression.ArithmeticExpression arithmeticExpression ->
                    anonotateTypesAtArithmeticExpression(arithmeticExpression);
            case Expression.Term term -> annotateTypesAtTerm(term);
            case Expression.Factor factor -> annotateTypesAtFactor(factor);
            case Expression.FunctionCall functionCall -> {
                TypedTreeNode.TypedIdentifier typedIdentifier =
                        new TypedTreeNode.TypedIdentifier(functionCall.functionName(), new FunType.TypeVariable());
                List<TypedTreeNode.Expression> arguments = functionCall.arguments().stream()
                        .map(TypeAnnotate::annotateTypesAtExpression)
                        .toList();
                yield new TypedTreeNode.FunctionCall(typedIdentifier, arguments);
            }
            case Expression.PrintlnExpression ignored -> new TypedTreeNode.NotImplemented();
            case Expression.StringLiteral stringLiteral ->
                    new TypedTreeNode.StringLiteral(stringLiteral.value(), new FunType.StringType());
        };
    }

    private static TypedTreeNode.ArithmeticExpression anonotateTypesAtArithmeticExpression(
            Expression.ArithmeticExpression arithmeticExpression
    ) {
        return switch (arithmeticExpression) {
            case Expression.BinaryArithmeticExpression binaryArithmeticExpression -> {
                TypedTreeNode.ArithmeticExpression.Operation operation =
                        switch (binaryArithmeticExpression.operation()) {
                            case ADD -> TypedTreeNode.ArithmeticExpression.Operation.ADD;
                            case SUBTRACT -> TypedTreeNode.ArithmeticExpression.Operation.SUBTRACT;
                        };
                yield new TypedTreeNode.BinaryArithmeticExpression(
                        new FunType.TypeVariable(),
                        annotateTypesAtTerm(binaryArithmeticExpression.left()),
                        annotateTypesAtTerm(binaryArithmeticExpression.right()),
                        operation
                );
            }
            case Expression.Operand operand ->
                    new TypedTreeNode.Operand(new FunType.TypeVariable(), annotateTypesAtTerm(operand.left()));
        };
    }

    private static TypedTreeNode.Term annotateTypesAtTerm(Expression.Term term) {
        return switch (term) {
            case Expression.BinaryTerm binaryTerm -> {
                TypedTreeNode.Term.Operation operation = switch (binaryTerm.operation()) {
                    case DIVIDE -> TypedTreeNode.Term.Operation.DIVIDE;
                    case MULTIPLY -> TypedTreeNode.Term.Operation.MULTIPLY;
                };
                yield new TypedTreeNode.BinaryTerm(
                        new FunType.TypeVariable(),
                        annotateTypesAtFactor(binaryTerm.left()),
                        annotateTypesAtFactor(binaryTerm.right()),
                        operation
                );
            }
            case Expression.SimpleTerm simpleTerm ->
                    new TypedTreeNode.SimpleTerm(new FunType.TypeVariable(), annotateTypesAtFactor(simpleTerm.left()));
        };
    }

    private static TypedTreeNode.Factor annotateTypesAtFactor(Expression.Factor factor) {
        return switch (factor) {
            case Expression.IntegerLiteral integerLiteral ->
                    new TypedTreeNode.IntegerLiteral(integerLiteral.literal(), new FunType.IntType());
            case Expression.ParenthesizedExpression parenthesizedExpression -> {
                TypedTreeNode.ArithmeticExpression typedExpression =
                        (TypedTreeNode.ArithmeticExpression) annotateTypesAtExpression(parenthesizedExpression.arithmeticExpression());
                yield new TypedTreeNode.ParenthesizedExpression(typedExpression);
            }
        };
    }

}
