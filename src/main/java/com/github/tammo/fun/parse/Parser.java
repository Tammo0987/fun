package com.github.tammo.fun.parse;

import com.github.tammo.fun.ast.SyntaxNode.CompilationUnit;

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
