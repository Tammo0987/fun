package com.github.tammo.fun.backend.codegen;

import com.github.tammo.fun.frontend.type.Type;
import com.github.tammo.fun.frontend.type.TypedTreeNode;
import com.github.tammo.fun.frontend.type.TypedTreeNode.CompilationUnit;
import com.github.tammo.fun.frontend.type.TypedTreeNode.Expression;
import com.github.tammo.fun.frontend.type.TypedTreeNode.PrintExpression;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ByteCodeGenerator implements CodeGenerator {

    @Override
    public byte[] generate(CompilationUnit programm) {
        final var classDeclaration = programm.classDeclaration();
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        classWriter.visit(
                V17,
                ACC_PUBLIC,
                programm.fullyQualifiedClassName(),
                null,
                "java/lang/Object",
                new String[]{}
        );

        classDeclaration.effects()
                .forEach(effectDeclaration -> writeMethodToClass(
                        effectDeclaration.name(),
                        effectDeclaration.body(),
                        classWriter
                ));

        classDeclaration.functions()
                .forEach(functionDeclaration -> writeMethodToClass(
                        functionDeclaration.name(),
                        functionDeclaration.body(),
                        classWriter
                ));

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private void writeMethodToClass(
            TypedTreeNode.TypedIdentifier typedIdentifier,
            Expression body,
            ClassWriter classWriter
    ) {
        String descriptor = TypeMapping.mapFunctionType(typedIdentifier.type());
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                typedIdentifier.identifier(),
                descriptor,
                null,
                new String[]{}
        );
        methodVisitor.visitCode();

        writeExpression(body, methodVisitor);

        writeReturn(typedIdentifier.type(), methodVisitor);

        methodVisitor.visitEnd();
        methodVisitor.visitMaxs(0, 0);
    }

    private static void writeExpression(Expression body, MethodVisitor methodVisitor) {
        switch (body) {
            case PrintExpression printlnExpression -> {
                methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                writeExpression(printlnExpression.expression(), methodVisitor);
                final var descriptor = "(" + TypeMapping.mapType(printlnExpression.expression().type()) + ")V";
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", descriptor, false);
            }
            case Expression.ArithmeticExpression expression ->
                    new ArithmeticExpressionWriter(methodVisitor).writeArithmeticExpression(expression);
            case Expression.FunctionCall functionCall -> {
                functionCall.arguments().forEach(argument -> writeExpression(argument, methodVisitor));
                String descriptor = TypeMapping.mapFunctionType(functionCall.type());
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        "com/github/tammo/Main", // TODO replace with dynamic owner
                        functionCall.name().identifier(),
                        descriptor,
                        false
                );
            }
            case Expression.StringLiteral stringLiteral -> methodVisitor.visitLdcInsn(stringLiteral.value());
            case Expression.Factor factor -> new ArithmeticExpressionWriter(methodVisitor).writeFactor(factor);
            case Expression.Term term -> new ArithmeticExpressionWriter(methodVisitor).writeTerm(term);
            case TypedTreeNode.NotImplemented ignored -> {
            }
        }
    }

    private void writeReturn(Type functionType, MethodVisitor methodVisitor) {
        switch (functionType) {
            case Type.Types.Unit -> methodVisitor.visitInsn(RETURN);
            case Type.Types.Int, Type.Types.Boolean -> methodVisitor.visitInsn(IRETURN);
            case Type.Types.String, Type.Types.StringArray -> methodVisitor.visitInsn(ARETURN);
            case Type.FunctionType functionTypeParameter ->
                    writeReturn(functionTypeParameter.returnType(), methodVisitor);
            case Type.TypeVariable ignored -> throw new IllegalStateException("Unexpected type variable");
        }
    }
}
