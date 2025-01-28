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
                List<TypedTreeNode.FunctionDeclaration> functionDeclarations = classDeclaration.functionDeclarations()
                        .stream()
                        .map(TypeAnnotate::annotateTypes)
                        .map(TypedTreeNode.FunctionDeclaration.class::cast)
                        .toList();
                yield new TypedTreeNode.ClassDeclaration(
                        classDeclaration.name(),
                        effectDeclarations,
                        functionDeclarations
                );
            }
            case SyntaxNode.CompilationUnit compilationUnit -> new TypedTreeNode.CompilationUnit(
                    compilationUnit.namespaceDeclaration()
                            .map(SyntaxNode.NamespaceDeclaration::qualifierIdentifier)
                            .map(TypedTreeNode.NamespaceDeclaration::new),
                    ((TypedTreeNode.ClassDeclaration) annotateTypes(compilationUnit.classDeclaration()))
            );
            case SyntaxNode.EffectDeclaration effectDeclaration -> {
                List<TypedTreeNode.Parameter> typedParameter = effectDeclaration.parameters().parameters().stream()
                        .map(TypeAnnotate::annotateTypes)
                        .map(TypedTreeNode.Parameter.class::cast)
                        .toList();
                TypedTreeNode.Expression typedBody = (TypedTreeNode.Expression) annotateTypes(effectDeclaration.body());

                Type effectType = Type.fromString(effectDeclaration.returnType());

                for (int i = effectDeclaration.parameters().parameters().size() - 1; i >= 0; i--) {
                    TypedTreeNode.Parameter parameter = typedParameter.get(i);
                    effectType = new Type.FunctionType(parameter.identifier().type(), effectType);
                }

                yield new TypedTreeNode.EffectDeclaration(
                        new TypedTreeNode.TypedIdentifier(effectDeclaration.name(), effectType),
                        typedParameter,
                        typedBody
                );
            }
            case SyntaxNode.ExposeDeclaration ignore -> new TypedTreeNode.NotImplemented();
            case SyntaxNode.NamespaceDeclaration namespaceDeclaration ->
                    new TypedTreeNode.NamespaceDeclaration(namespaceDeclaration.qualifierIdentifier());
            case SyntaxNode.UseDeclaration ignore -> new TypedTreeNode.NotImplemented();
            case SyntaxNode.Parameter parameter -> new TypedTreeNode.Parameter(
                    new TypedTreeNode.TypedIdentifier(parameter.name(), Type.fromString(parameter.type()))
            );
            case SyntaxNode.ParameterList ignored -> new TypedTreeNode.NotImplemented();
            case SyntaxNode.FunctionDeclaration functionDeclaration -> {
                List<TypedTreeNode.Parameter> parameters = functionDeclaration.parameters().parameters().stream()
                        .map(TypeAnnotate::annotateTypes)
                        .map(TypedTreeNode.Parameter.class::cast)
                        .toList();

                Type functionType = Type.fromString(functionDeclaration.returnType());

                for (int i = functionDeclaration.parameters().parameters().size() - 1; i >= 0; i--) {
                    TypedTreeNode.Parameter parameter = parameters.get(i);
                    functionType = new Type.FunctionType(parameter.identifier().type(), functionType);
                }

                TypedTreeNode.TypedIdentifier typedIdentifier = new TypedTreeNode.TypedIdentifier(
                        functionDeclaration.name(),
                        functionType
                );

                TypedTreeNode.Expression body = annotateTypesAtExpression(functionDeclaration.body());
                yield new TypedTreeNode.FunctionDeclaration(typedIdentifier, parameters, body);
            }
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
                        new TypedTreeNode.TypedIdentifier(functionCall.functionName(), new Type.TypeVariable());
                List<TypedTreeNode.Expression> arguments = functionCall.arguments().stream()
                        .map(TypeAnnotate::annotateTypesAtExpression)
                        .toList();
                yield new TypedTreeNode.FunctionCall(typedIdentifier, arguments);
            }
            case Expression.PrintlnExpression printlnExpression ->
                    new TypedTreeNode.PrintExpression(annotateTypesAtExpression(printlnExpression.expression()));
            case Expression.StringLiteral stringLiteral -> new TypedTreeNode.StringLiteral(stringLiteral.value());
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
                        new Type.TypeVariable(),
                        annotateTypesAtTerm(binaryArithmeticExpression.left()),
                        annotateTypesAtTerm(binaryArithmeticExpression.right()),
                        operation
                );
            }
            case Expression.Operand operand ->
                    new TypedTreeNode.Operand(new Type.TypeVariable(), annotateTypesAtTerm(operand.left()));
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
                        new Type.TypeVariable(),
                        annotateTypesAtFactor(binaryTerm.left()),
                        annotateTypesAtFactor(binaryTerm.right()),
                        operation
                );
            }
            case Expression.SimpleTerm simpleTerm ->
                    new TypedTreeNode.SimpleTerm(new Type.TypeVariable(), annotateTypesAtFactor(simpleTerm.left()));
        };
    }

    private static TypedTreeNode.Factor annotateTypesAtFactor(Expression.Factor factor) {
        return switch (factor) {
            case Expression.IntegerLiteral integerLiteral -> new TypedTreeNode.IntegerLiteral(integerLiteral.literal());
            case Expression.ParenthesizedExpression parenthesizedExpression -> {
                TypedTreeNode.ArithmeticExpression typedExpression =
                        (TypedTreeNode.ArithmeticExpression) annotateTypesAtExpression(parenthesizedExpression.arithmeticExpression());
                yield new TypedTreeNode.ParenthesizedExpression(typedExpression);
            }
        };
    }

}
