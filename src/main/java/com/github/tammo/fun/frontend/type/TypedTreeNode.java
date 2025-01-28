package com.github.tammo.fun.frontend.type;

import java.util.List;

public sealed interface TypedTreeNode {

    record NotImplemented() implements Expression {}

    record CompilationUnit(ObjectDeclaration objectDeclaration) implements TypedTreeNode {
    }

    record ObjectDeclaration(
            String name,
            List<EffectDeclaration> effects
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

    record Parameter(TypedIdentifier identifier) implements TypedTreeNode {

    }

    sealed interface Expression extends TypedTreeNode {

    }

    record StringLiteral(String value, FunType.StringType type) implements Expression {

    }

    record FunctionCall(TypedIdentifier name, List<Expression> arguments) implements Expression {

    }

    sealed interface ArithmeticExpression extends Expression {
        enum Operation {
            ADD,
            SUBTRACT
        }
    }

    record Operand(FunType type, Term left) implements ArithmeticExpression {

    }

    record BinaryArithmeticExpression(
            FunType type,
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

    record SimpleTerm(FunType type, Factor left) implements Term {

    }

    record BinaryTerm(FunType type, Factor left, Factor right, Term.Operation operation) implements Term {

    }

    sealed interface Factor extends Expression {

    }

    record IntegerLiteral(int literal, FunType.IntType type) implements Factor {

    }

    record ParenthesizedExpression(ArithmeticExpression arithmeticExpression) implements Factor {

    }

    record TypedIdentifier(String identifier, FunType type) implements TypedTreeNode {

    }


}
