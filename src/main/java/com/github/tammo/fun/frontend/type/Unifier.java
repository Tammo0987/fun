package com.github.tammo.fun.frontend.type;

import java.util.HashMap;
import java.util.Set;

public class Unifier {

    private Substitutions substitutions = Substitutions.empty();

    public void unifyAll(Set<Constraint> constraints) {
        constraints.forEach(constraint -> unify(constraint.left(), constraint.right()));
    }

    private void unify(Type left, Type right) {
        left = applySubstitution(left);
        right = applySubstitution(right);

        if (left.equals(right)) {
            return;
        }

        if (left instanceof Type.TypeVariable varLeft) {
            unifyVariable(varLeft, right);
        } else if (right instanceof Type.TypeVariable varRight) {
            unifyVariable(varRight, left);
        } else if (left instanceof Type.FunctionType functionLeft && right instanceof Type.FunctionType functionRight) {
            unify(functionLeft.parameter(), functionRight.parameter());
            unify(functionLeft.returnType(), functionRight.returnType());
        } else {
            throw new IllegalStateException("Cannot unify " + left + " with " + right);
        }
    }

    private void unifyVariable(Type.TypeVariable variable, Type other) {
        Type currentSubstitution = substitutions.substitutions().get(variable);
        if (currentSubstitution != null) {
            unify(currentSubstitution, other);
            return;
        }

        if (other instanceof Type.TypeVariable otherVariable) {
            Type currentOtherSubstitution = substitutions.substitutions().get(otherVariable);
            if (currentOtherSubstitution != null) {
                unifyVariable(variable, currentOtherSubstitution);
                return;
            }
        }

        if (occursCheck(variable, other)) {
            throw new IllegalStateException("Occurs check failed: " + variable + " in " + other);
        }

        final var newSubstitutions = new HashMap<>(substitutions.substitutions());

        newSubstitutions.put(variable, other);

        substitutions = new Substitutions(newSubstitutions);
    }

    private boolean occursCheck(Type.TypeVariable variable, Type other) {
        if (other.equals(variable)) {
            return true;
        }

        if (other instanceof Type.FunctionType(Type parameter, Type returnType)) {
            return occursCheck(variable, parameter) || occursCheck(variable, returnType);
        }

        return false;
    }

    public Type applySubstitution(Type type) {
        // If it's a variable and we have a substitution, follow that chain
        if (type instanceof Type.TypeVariable var) {
            Type subst = substitutions.substitutions().get(var);
            if (subst != null) {
                // Recursively apply in case there's a chain
                return applySubstitution(subst);
            }
        } else if (type instanceof Type.FunctionType(Type parameter, Type returnType)) {
            // For function types, apply substitution recursively
            Type param = applySubstitution(parameter);
            Type ret = applySubstitution(returnType);
            return new Type.FunctionType(param, ret);
        }
        // If there's no substitution, just return as is
        return type;

    }

    public Substitutions getSubstitutions() {
        return substitutions;
    }
}
