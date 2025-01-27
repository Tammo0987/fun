package com.github.tammo.fun.ast;

import java.util.List;

public sealed interface Expression extends SyntaxNode {

    record PrintlnExpression(Expression expression) implements Expression {
    }

    record StringLiteral(String value) implements Expression {

    }

    record FunctionCall(String functionName, List<Expression> arguments) implements Expression {

    }

    sealed interface ArithmeticExpression extends Expression {
        enum Operation {
            ADD,
            SUBTRACT
        }
    }

    record Operand(Term left) implements ArithmeticExpression {

    }

    record BinaryArithmeticExpression(
            Term left,
            Term right,
            ArithmeticExpression.Operation operation
    ) implements ArithmeticExpression {

    }

    sealed interface Term extends SyntaxNode {
        enum Operation {
            MULTIPLY,
            DIVIDE
        }
    }

    record SimpleTerm(Factor left) implements Term {

    }

    record BinaryTerm(Factor left, Factor right, Term.Operation operation) implements Term {

    }

    sealed interface Factor extends SyntaxNode {

    }

    record IntegerLiteral(int literal) implements Factor {

    }

    record ParenthesizedExpression(ArithmeticExpression arithmeticExpression) implements Factor {

    }

}
