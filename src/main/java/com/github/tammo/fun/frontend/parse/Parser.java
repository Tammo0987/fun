package com.github.tammo.fun.frontend.parse;

import com.github.tammo.fun.frontend.ast.SyntaxNode.CompilationUnit;

/**
 * Parses input to an abstract syntax tree as {@link CompilationUnit}.
 */
public interface Parser {

    /**
     * Actually parses the input.
     *
     * @param input The input to parse.
     * @return An abstract syntax tree as {@link CompilationUnit}.
     */
    CompilationUnit parse(String input);

}
