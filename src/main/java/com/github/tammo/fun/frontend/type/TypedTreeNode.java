package com.github.tammo.fun.frontend.type;

import java.util.List;
import java.util.Optional;

public sealed interface TypedTreeNode {

    record NotImplemented() implements Expression {
        @Override
        public Type type() {
            return Type.Types.Unit;
        }
    }

    record CompilationUnit(
            Optional<NamespaceDeclaration> namespaceDeclaration,
            ClassDeclaration classDeclaration
    ) implements TypedTreeNode {

        public String fullyQualifiedClassName() {
            final var qualifiedNamespaceName = namespaceDeclaration.map(NamespaceDeclaration::identifier)
                    .map(i -> i + "/")
                    .orElse("");
            return qualifiedNamespaceName + classDeclaration.name();
        }

    }

    record ClassDeclaration(
            String name,
            List<EffectDeclaration> effects,
            List<FunctionDeclaration> functions
    ) implements TypedTreeNode {

    }

    record NamespaceDeclaration(String identifier) implements TypedTreeNode {

    }

    record EffectDeclaration(
            TypedIdentifier name,
            List<Parameter> parameters,
            Expression body
    ) implements TypedTreeNode {

    }

    record FunctionDeclaration(
            TypedIdentifier name,
            List<Parameter> parameters,
            Expression body
    ) implements TypedTreeNode {

    }

    record Parameter(TypedIdentifier identifier) implements TypedTreeNode {

    }

    sealed interface Expression extends TypedTreeNode {
        Type type();
    }

    record PrintExpression(Expression expression) implements Expression {
        @Override
        public Type type() {
            return Type.Types.Unit;
        }
    }

    record StringLiteral(String value) implements Expression {
        @Override
        public Type type() {
            return Type.Types.String;
        }
    }

    record FunctionCall(TypedIdentifier name, List<Expression> arguments) implements Expression {

        @Override
        public Type type() {
            if (arguments.isEmpty()) {
                return name.type();
            } else {
                final var reversedArguments = List.copyOf(arguments).reversed();
                Type start = name.type();
                for (final var argument : reversedArguments) {
                    start = new Type.FunctionType(argument.type(), start);
                }
                return start;
            }
        }
    }

    sealed interface ArithmeticExpression extends Expression {
        enum Operation {
            ADD,
            SUBTRACT
        }
    }

    record Operand(Type type, Term left) implements ArithmeticExpression {

    }

    record BinaryArithmeticExpression(
            Type type,
            Term left,
            Term right,
            ArithmeticExpression.Operation operation
    ) implements ArithmeticExpression {

    }

    sealed interface Term extends Expression {
        enum Operation {
            MULTIPLY,
            DIVIDE
        }
    }

    record SimpleTerm(Type type, Factor left) implements Term {

    }

    record BinaryTerm(Type type, Factor left, Factor right, Term.Operation operation) implements Term {

    }

    sealed interface Factor extends Expression {

    }

    record IntegerLiteral(int literal) implements Factor {

        @Override
        public Type type() {
            return Type.Types.Int;
        }

    }

    record ParenthesizedExpression(ArithmeticExpression arithmeticExpression) implements Factor {

        @Override
        public Type type() {
            return arithmeticExpression.type();
        }

    }

    record TypedIdentifier(String identifier, Type type) implements TypedTreeNode {

    }


}
