package com.github.tammo.fun.frontend.type;

import java.util.Map;

public record Substitutions(Map<Type.TypeVariable, Type> substitutions) {

    public static Substitutions empty() {
        return new Substitutions(Map.of());
    }

}
