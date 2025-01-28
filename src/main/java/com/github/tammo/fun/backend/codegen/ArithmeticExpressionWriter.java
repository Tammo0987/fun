package com.github.tammo.fun.backend.codegen;

import com.github.tammo.fun.frontend.type.TypedTreeNode;
import com.github.tammo.fun.frontend.type.TypedTreeNode.*;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ArithmeticExpressionWriter {

    private final MethodVisitor methodVisitor;

    public ArithmeticExpressionWriter(MethodVisitor methodVisitor) {
        this.methodVisitor = methodVisitor;
    }

    void writeArithmeticExpression(TypedTreeNode.ArithmeticExpression expression) {
        switch (expression) {
            case Operand operand -> writeTerm(operand.left());
            case BinaryArithmeticExpression binaryExpression -> {
                writeTerm(binaryExpression.left());
                writeTerm(binaryExpression.right());
                switch (binaryExpression.operation()) {
                    case ADD -> methodVisitor.visitInsn(Opcodes.IADD);
                    case SUBTRACT -> methodVisitor.visitInsn(Opcodes.ISUB);
                }
            }
        }
    }

    void writeTerm(TypedTreeNode.Term term) {
        switch (term) {
            case SimpleTerm simpleTerm -> writeFactor(simpleTerm.left());
            case BinaryTerm binaryTerm -> {
                writeFactor(binaryTerm.left());
                writeFactor(binaryTerm.right());
                switch (binaryTerm.operation()) {
                    case MULTIPLY -> methodVisitor.visitInsn(Opcodes.IMUL);
                    case DIVIDE -> methodVisitor.visitInsn(Opcodes.IDIV);
                }
            }
        }
    }

    void writeFactor(Factor factor) {
        switch (factor) {
            case IntegerLiteral integerLiteral -> methodVisitor.visitLdcInsn(integerLiteral.literal());
            case ParenthesizedExpression parenthesizedExpression ->
                    writeArithmeticExpression(parenthesizedExpression.arithmeticExpression());
        }
    }
}
