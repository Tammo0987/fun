package com.github.tammo.fun.frontend.type;

public sealed interface Type {

    static Type fromString(String type) {
        return switch (type) {
            case "Int" -> Types.Int;
            case "Boolean" -> Types.Boolean;
            case "String" -> Types.String;
            case "String[]" -> Types.StringArray;
            case "Unit" -> Types.Unit;
            default -> new TypeVariable();
        };
    }

    enum Types implements Type {
        Int,
        Boolean,
        String,
        Unit,
        StringArray
    }

    record FunctionType(Type parameter, Type returnType) implements Type {

    }

    record TypeVariable() implements Type {
    }

}
