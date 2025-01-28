package com.github.tammo.fun.frontend.ast;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public sealed interface SyntaxNode permits Expression,
        Expression.Factor,
        Expression.Term,
        SyntaxNode.ClassDeclaration,
        SyntaxNode.CompilationUnit,
        SyntaxNode.EffectDeclaration,
        SyntaxNode.ExposeDeclaration,
        SyntaxNode.FunctionDeclaration,
        SyntaxNode.NamespaceDeclaration,
        SyntaxNode.Parameter,
        SyntaxNode.ParameterList,
        SyntaxNode.UseDeclaration {

    record CompilationUnit(
            Optional<NamespaceDeclaration> namespaceDeclaration,
            List<UseDeclaration> useDeclarations,
            List<ExposeDeclaration> exposeDeclarations,
            ClassDeclaration classDeclaration
    ) implements SyntaxNode {

        public String fullyQualifiedClassName() {
            final var qualifiedNamespaceName = namespaceDeclaration.map(NamespaceDeclaration::qualifierIdentifier)
                    .map(i -> i + "/")
                    .orElse("");
            return qualifiedNamespaceName + classDeclaration.name();
        }
    }

    record NamespaceDeclaration(String qualifierIdentifier) implements SyntaxNode {
    }

    record UseDeclaration(Set<String> qualifierIdentifiers) implements SyntaxNode {
    }

    sealed interface ExposeDeclaration extends SyntaxNode {
    }

    record ExposeNamespace(Set<String> identifiers) implements ExposeDeclaration {
    }

    record ExposeIdentifiers(Set<String> identifiers) implements ExposeDeclaration {
    }

    record ClassDeclaration(
            String name,
            ParameterList parameters,
            List<EffectDeclaration> effectDeclarations,
            List<FunctionDeclaration> functionDeclarations
    ) implements SyntaxNode {
    }

    record EffectDeclaration(
            String name,
            ParameterList parameters,
            String returnType,
            Expression body
    ) implements SyntaxNode {
    }

    record FunctionDeclaration(
            String name,
            ParameterList parameters,
            String returnType,
            Expression body
    ) implements SyntaxNode {

    }

    record ParameterList(List<Parameter> parameters) implements SyntaxNode {

    }

    record Parameter(String name, String type) implements SyntaxNode {
    }

}
