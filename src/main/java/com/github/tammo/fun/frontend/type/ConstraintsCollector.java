package com.github.tammo.fun.frontend.type;

import com.github.tammo.fun.frontend.type.TypedTreeNode.*;

import java.util.HashSet;
import java.util.Set;

public class ConstraintsCollector {

    public static Set<Constraint> collect(TypedTreeNode typedTreeNode) {
        return switch (typedTreeNode) {
            case CompilationUnit compilationUnit -> collect(compilationUnit.classDeclaration());
            case ClassDeclaration classDeclaration -> {
                final var constraints = new HashSet<Constraint>();
                classDeclaration.effects().forEach(effect -> constraints.addAll(collect(effect)));
                classDeclaration.functions().forEach(function -> constraints.addAll(collect(function)));
                yield constraints;
            }
            case EffectDeclaration effectDeclaration -> {
                final var constraints = new HashSet<Constraint>();
                effectDeclaration.parameters().forEach(parameter -> constraints.addAll(collect(parameter)));
                constraints.addAll(collect(effectDeclaration.body()));
                constraints.add(new Constraint(unwrapFunctionReturnType(effectDeclaration.name().type()), effectDeclaration.body().type()));
                yield constraints;
            }
            case FunctionDeclaration functionDeclaration -> {
                final var constraints = new HashSet<Constraint>();
                functionDeclaration.parameters().forEach(parameter -> constraints.addAll(collect(parameter)));
                constraints.addAll(collect(functionDeclaration.body()));
                constraints.add(new Constraint(unwrapFunctionReturnType(functionDeclaration.name().type()), functionDeclaration.body().type()));
                yield constraints;
            }
            case Parameter parameter -> collect(parameter.identifier());
            case Expression expression -> collectConstraintsForExpression(expression);
            case NamespaceDeclaration ignored -> Set.of();
            case TypedIdentifier ignored -> Set.of();
        };
    }

    private static Set<Constraint> collectConstraintsForExpression(Expression expression) {
        return switch (expression) {
            case ArithmeticExpression arithmeticExpression -> switch (arithmeticExpression) {
                case BinaryArithmeticExpression binaryArithmeticExpression -> {
                    final var constraints = new HashSet<Constraint>();
                    constraints.addAll(collect(binaryArithmeticExpression.left()));
                    constraints.addAll(collect(binaryArithmeticExpression.right()));
                    constraints.add(new Constraint(binaryArithmeticExpression.type(), Type.Types.Int));
                    constraints.add(new Constraint(binaryArithmeticExpression.left().type(), Type.Types.Int));
                    constraints.add(new Constraint(binaryArithmeticExpression.right().type(), Type.Types.Int));
                    yield constraints;
                }
                case Operand operand -> {
                    final var constraints = new HashSet<>(collect(operand.left()));
                    constraints.add(new Constraint(operand.type(), Type.Types.Int));
                    constraints.add(new Constraint(operand.left().type(), Type.Types.Int));
                    yield constraints;
                }
            };
            case Factor factor -> switch (factor) {
                case IntegerLiteral ignored -> Set.of();
                case ParenthesizedExpression pe -> {
                    final var constraints = new HashSet<>(collectConstraintsForExpression(pe.arithmeticExpression()));
                    constraints.add(new Constraint(pe.type(), Type.Types.Int));
                    yield constraints;
                }
            };
            case FunctionCall functionCall -> {
                final var constraints = new HashSet<Constraint>();
                functionCall.arguments().forEach(argument -> constraints.addAll(collect(argument)));
                // TODO maybe add more constraints
                yield constraints;
            }
            case NotImplemented ignored -> Set.of();
            case PrintExpression printExpression -> collectConstraintsForExpression(printExpression.expression());
            case StringLiteral ignored -> Set.of();
            case Term term -> switch (term) {
                case SimpleTerm simpleTerm -> {
                    final var constraints = new HashSet<>(collectConstraintsForExpression(simpleTerm.left()));

                    constraints.add(new Constraint(simpleTerm.type(), Type.Types.Int));
                    constraints.add(new Constraint(simpleTerm.left().type(), Type.Types.Int));

                    yield constraints;
                }
                case BinaryTerm binaryTerm -> {
                    final var constraints = new HashSet<Constraint>();

                    constraints.addAll(collect(binaryTerm.left()));
                    constraints.addAll(collect(binaryTerm.right()));

                    constraints.add(new Constraint(binaryTerm.type(), Type.Types.Int));
                    constraints.add(new Constraint(binaryTerm.left().type(), Type.Types.Int));
                    constraints.add(new Constraint(binaryTerm.right().type(), Type.Types.Int));

                    yield constraints;
                }
            };
        };
    }

    private static Type unwrapFunctionReturnType(Type type) {
        if (type instanceof Type.FunctionType functionType) {
            return unwrapFunctionReturnType(functionType.returnType());
        } else {
            return type;
        }
    }
}