package com.github.tammo.fun.frontend.type;

import com.github.tammo.fun.frontend.type.TypedTreeNode.ArithmeticExpression;
import com.github.tammo.fun.frontend.type.TypedTreeNode.BinaryArithmeticExpression;
import com.github.tammo.fun.frontend.type.TypedTreeNode.NotImplemented;
import com.github.tammo.fun.frontend.type.TypedTreeNode.ParenthesizedExpression;

public class SubstitutionApplier {

    private final Unifier unifier;

    public SubstitutionApplier(Unifier unifier) {
        this.unifier = unifier;
    }

    public TypedTreeNode apply(TypedTreeNode node) {
        if (node == null) {
            return null;
        }

        // Rebuild the node based on its specific subtype
        return switch (node) {
            // ------------------------------------------------
            // Example: CompilationUnit
            // ------------------------------------------------
            case TypedTreeNode.CompilationUnit cu ->
                    new TypedTreeNode.CompilationUnit(
                            cu.namespaceDeclaration(),
                            (TypedTreeNode.ClassDeclaration) apply(cu.classDeclaration())
                    );

            // ------------------------------------------------
            // Example: ClassDeclaration
            // ------------------------------------------------
            case TypedTreeNode.ClassDeclaration cd -> {
                var newEffects = cd.effects().stream()
                        .map(this::apply)
                        .map(TypedTreeNode.EffectDeclaration.class::cast)
                        .toList();

                var newFunctions = cd.functions().stream()
                        .map(this::apply)
                        .map(TypedTreeNode.FunctionDeclaration.class::cast)
                        .toList();

                // Build the new ClassDeclaration
                yield new TypedTreeNode.ClassDeclaration(cd.name(), newEffects, newFunctions);
            }

            // ------------------------------------------------
            // Example: EffectDeclaration
            // ------------------------------------------------
            case TypedTreeNode.EffectDeclaration ed -> {
                TypedTreeNode.TypedIdentifier newTypedIdentifier = (TypedTreeNode.TypedIdentifier) apply(ed.name());

                var newParams = ed.parameters().stream()
                        .map(this::apply)
                        .map(TypedTreeNode.Parameter.class::cast)
                        .toList();

                TypedTreeNode.Expression newBody = (TypedTreeNode.Expression) apply(ed.body());

                yield new TypedTreeNode.EffectDeclaration(
                        newTypedIdentifier,
                        newParams,
                        newBody
                );
            }

            // ------------------------------------------------
            // Example: FunctionDeclaration
            // ------------------------------------------------
            case TypedTreeNode.FunctionDeclaration fd -> {
                TypedTreeNode.TypedIdentifier newTypedIdentifier = (TypedTreeNode.TypedIdentifier) apply(fd.name());

                var newParams = fd.parameters().stream()
                        .map(this::apply)
                        .map(TypedTreeNode.Parameter.class::cast)
                        .toList();

                TypedTreeNode.Expression newBody = (TypedTreeNode.Expression) apply(fd.body());
                yield new TypedTreeNode.FunctionDeclaration(
                        newTypedIdentifier,
                        newParams,
                        newBody
                );
            }

            // ------------------------------------------------
            // Example: Parameter
            // ------------------------------------------------
            case TypedTreeNode.Parameter param -> {
                TypedTreeNode.TypedIdentifier typedIdentifier = (TypedTreeNode.TypedIdentifier) apply(param.identifier());
                yield new TypedTreeNode.Parameter(typedIdentifier);
            }

            // ------------------------------------------------
            // Example: TypedIdentifier
            // ------------------------------------------------
            case TypedTreeNode.TypedIdentifier ti -> {
                // Possibly no children to transform, so just rebuild with newType
                final var newType = unifier.applySubstitution(ti.type());
                yield new TypedTreeNode.TypedIdentifier(ti.identifier(), newType);
            }

            // ------------------------------------------------
            // Example: Expression subtypes
            // ------------------------------------------------
            case TypedTreeNode.Expression expr -> switch (expr) {
                // IntegerLiteral
                case TypedTreeNode.IntegerLiteral intLit -> intLit;

                // StringLiteral
                case TypedTreeNode.StringLiteral strLit -> strLit;

                // PrintExpression
                case TypedTreeNode.PrintExpression printExpr -> {
                    TypedTreeNode.Expression newInner = (TypedTreeNode.Expression) apply(printExpr.expression());
                    yield new TypedTreeNode.PrintExpression(newInner);
                }

                // FunctionCall
                case TypedTreeNode.FunctionCall funcCall -> {
                    TypedTreeNode.TypedIdentifier typedIdentifier = (TypedTreeNode.TypedIdentifier) apply(funcCall.name());
                    var newArguments = funcCall.arguments().stream()
                            .map(this::apply)
                            .map(TypedTreeNode.Expression.class::cast)
                            .toList();
                    yield new TypedTreeNode.FunctionCall(
                            typedIdentifier,
                            newArguments
                    );
                }

                // ArithmeticExpression
                case ArithmeticExpression aexpr -> switch (aexpr) {
                    case TypedTreeNode.Operand operand -> {
                        var newType = unifier.applySubstitution(operand.type());
                        var newLeft = apply(operand.left());
                        yield new TypedTreeNode.Operand(newType, (TypedTreeNode.Term) newLeft);
                    }
                    case BinaryArithmeticExpression bae -> {
                        var newType = unifier.applySubstitution(bae.type());
                        var newLeft = apply(bae.left());
                        var newRight = apply(bae.right());
                        yield new BinaryArithmeticExpression(
                                newType,
                                (TypedTreeNode.Term) newLeft,
                                (TypedTreeNode.ArithmeticExpression) newRight,
                                bae.operation()
                        );
                    }
                };
                case NotImplemented notImplemented -> notImplemented;
                case ParenthesizedExpression parenthesizedExpression ->
                        new ParenthesizedExpression((ArithmeticExpression) apply(parenthesizedExpression.arithmeticExpression()));
                case TypedTreeNode.Term term -> switch (term) {
                    case TypedTreeNode.SimpleTerm simpleTerm -> {
                        final var newType = unifier.applySubstitution(simpleTerm.type());
                        final var newLeft = apply(simpleTerm.left());
                        yield new TypedTreeNode.SimpleTerm(newType, (TypedTreeNode.Factor) newLeft);
                    }
                    case TypedTreeNode.BinaryTerm binaryTerm -> {
                        final var newType = unifier.applySubstitution(binaryTerm.type());
                        final var newLeft = apply(binaryTerm.left());
                        final var newRight = apply(binaryTerm.right());
                        yield new TypedTreeNode.BinaryTerm(
                                newType,
                                (TypedTreeNode.Factor) newLeft,
                                (TypedTreeNode.Term) newRight,
                                binaryTerm.operation()
                        );
                    }
                };
            };
            case TypedTreeNode.NamespaceDeclaration namespaceDeclaration -> namespaceDeclaration;
        };
    }
}
