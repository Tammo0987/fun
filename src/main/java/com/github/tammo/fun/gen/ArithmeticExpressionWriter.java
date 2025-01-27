package com.github.tammo.fun.gen;

import com.github.tammo.fun.ast.Expression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ArithmeticExpressionWriter {

    private final MethodVisitor methodVisitor;

    public ArithmeticExpressionWriter(MethodVisitor methodVisitor) {
        this.methodVisitor = methodVisitor;
    }

    void writeArithmeticExpression(Expression.ArithmeticExpression expression) {
        switch (expression) {
            case Expression.Operand operand -> writeTerm(operand.left());
            case Expression.BinaryArithmeticExpression binaryExpression -> {
                writeTerm(binaryExpression.left());
                writeTerm(binaryExpression.right());
                switch (binaryExpression.operation()) {
                    case ADD -> methodVisitor.visitInsn(Opcodes.IADD);
                    case SUBTRACT -> methodVisitor.visitInsn(Opcodes.ISUB);
                }
            }
        }
    }

    private void writeTerm(Expression.Term term) {
        switch (term) {
            case Expression.SimpleTerm simpleTerm -> writeFactor(simpleTerm.left());
            case Expression.BinaryTerm binaryTerm -> {
                writeFactor(binaryTerm.left());
                writeFactor(binaryTerm.right());
                switch (binaryTerm.operation()) {
                    case MULTIPLY -> methodVisitor.visitInsn(Opcodes.IMUL);
                    case DIVIDE -> methodVisitor.visitInsn(Opcodes.IDIV);
                }
            }
        }
    }

    private void writeFactor(Expression.Factor factor) {
        switch (factor) {
            case Expression.IntegerLiteral integerLiteral -> methodVisitor.visitLdcInsn(integerLiteral.literal());
            case Expression.ParenthesizedExpression parenthesizedExpression ->
                    writeArithmeticExpression(parenthesizedExpression.arithmeticExpression());
        }
    }
}
