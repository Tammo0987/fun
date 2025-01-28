package com.github.tammo.fun.frontend.type;

public sealed interface FunType {

    static FunType fromString(String type) {
        return switch (type) {
            case "Int" -> new IntType();
            case "Boolean" -> new BooleanType();
            case "String" -> new StringType();
            default -> new TypeVariable();
        };
    }

    record IntType() implements FunType {

    }

    record BooleanType() implements FunType {

    }

    record StringType() implements FunType {

    }

    record FunctionType(FunType parameter, FunType returnType) implements FunType {

    }

    record TypeVariable() implements FunType {

    }

}
