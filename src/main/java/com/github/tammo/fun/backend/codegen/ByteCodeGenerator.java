package com.github.tammo.fun.backend.codegen;

import com.github.tammo.fun.frontend.ast.Expression;
import com.github.tammo.fun.frontend.ast.SyntaxNode;
import com.github.tammo.fun.frontend.ast.SyntaxNode.CompilationUnit;
import com.github.tammo.fun.frontend.ast.SyntaxNode.ParameterList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.stream.Collectors;

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

        classDeclaration.effectDeclarations()
                .forEach(effectDeclaration -> writeMethodToClass(
                        effectDeclaration.parameters(),
                        effectDeclaration.returnType(),
                        effectDeclaration.name(),
                        effectDeclaration.body(),
                        classWriter
                ));

        classDeclaration.functionDeclarations()
                .forEach(functionDeclaration -> writeMethodToClass(
                        functionDeclaration.parameters(),
                        functionDeclaration.returnType(),
                        functionDeclaration.name(),
                        functionDeclaration.body(),
                        classWriter
                ));

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private void writeMethodToClass(
            ParameterList parameters,
            String returnType,
            String name,
            Expression body,
            ClassWriter classWriter
    ) {
        final var parameterTypes = parameters.parameters().stream().map(SyntaxNode.Parameter::type)
                .map(TypeMapping::mapType)
                .collect(Collectors.joining());
        final var mappedReturnType = TypeMapping.mapType(returnType);

        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                name,
                "(" + parameterTypes + ")" + mappedReturnType,
                null,
                new String[]{}
        );
        methodVisitor.visitCode();

        writeExpression(body, methodVisitor);

        writeReturn(mappedReturnType, methodVisitor);

        methodVisitor.visitEnd();
        methodVisitor.visitMaxs(0, 0);
    }

    private static void writeExpression(Expression body, MethodVisitor methodVisitor) {
        switch (body) {
            case Expression.PrintlnExpression printlnExpression -> {
                methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                writeExpression(printlnExpression.expression(), methodVisitor);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            }
            case Expression.ArithmeticExpression expression ->
                    new ArithmeticExpressionWriter(methodVisitor).writeArithmeticExpression(expression);
            case Expression.FunctionCall functionCall -> {
                functionCall.arguments().forEach(argument -> writeExpression(argument, methodVisitor));
                // TODO add typechecker and type inference to have type information available
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        "com/github/tammo/Main",
                        functionCall.functionName(),
                        "()I",
                        false
                );
            }
            case Expression.StringLiteral stringLiteral -> methodVisitor.visitLdcInsn(stringLiteral.value());
        }
    }

    private void writeReturn(String returnType, MethodVisitor methodVisitor) {
        switch (returnType) {
            case "V" -> methodVisitor.visitInsn(RETURN);
            case "I", "B" -> methodVisitor.visitInsn(IRETURN);
            case "Ljava/lang/String;", "[Ljava/lang/String;" -> methodVisitor.visitInsn(ARETURN);
        }
    }
}
