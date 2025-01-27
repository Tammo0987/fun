package com.github.tammo.fun.gen;

public class TypeMapping {

    // TODO support more types
    static String mapType(String funType) {
        return switch (funType) {
            case "Unit" -> "V";
            case "Int" -> "I";
            case "Boolean" -> "B";
            case "String" -> "Ljava/lang/String;";
            case "String[]" -> "[Ljava/lang/String;";
            default -> throw new IllegalStateException("Unexpected type mapping for " + funType);
        };
    }

}
