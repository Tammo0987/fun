package com.github.tammo.fun.frontend.type;

import com.github.tammo.fun.frontend.ast.SyntaxNode;

public class TypeChecker {

    public static TypedTreeNode typeCheck(SyntaxNode tree) {
        final var annotatedTypedTree = TypeAnnotate.annotateTypes(tree);
        final var constraints = ConstraintsCollector.collect(annotatedTypedTree);

        final var unifier = new Unifier();
        unifier.unifyAll(constraints);

        return new SubstitutionApplier(unifier).apply(annotatedTypedTree);
    }

}
