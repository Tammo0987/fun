package com.github.tammo.fun.backend.codegen;

import com.github.tammo.fun.frontend.type.Type;

public class TypeMapping {

    static String mapType(Type type) {
        return switch (type) {
            case Type.Types.Unit -> "V";
            case Type.Types.Int -> "I";
            case Type.Types.Boolean -> "B";
            case Type.Types.String -> "Ljava/lang/String;";
            case Type.Types.StringArray -> "[Ljava/lang/String;";
            case Type.FunctionType functionType -> mapFunctionType(functionType);
            default -> throw new IllegalStateException("Unexpected type mapping for " + type);
        };
    }

    static String mapFunctionType(Type type) {
        if (type instanceof Type.FunctionType functionType) {
            return mapFunctionType(functionType);
        }
        return "()" + mapType(type);
    }

    private static String mapFunctionType(Type.FunctionType functionType) {
        return "(" + mapFunctionTypeParameter(functionType) + ")" + mapType(functionType.returnType());
    }

    private static String mapFunctionTypeParameter(Type.FunctionType functionType) {
        if (functionType.parameter() instanceof Type.FunctionType functionTypeParameter) {
            return mapFunctionTypeParameter(functionTypeParameter) + mapType(functionType.returnType());
        } else {
            return mapType(functionType.parameter());
        }
    }

}
