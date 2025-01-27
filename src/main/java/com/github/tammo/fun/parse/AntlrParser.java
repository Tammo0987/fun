package com.github.tammo.fun.parse;

import com.github.tammo.FunLexer;
import com.github.tammo.FunParser;
import com.github.tammo.fun.ast.SyntaxNode.CompilationUnit;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class AntlrParser implements Parser {

    private final ASTBuilder astBuilder = new ASTBuilder();

    @Override
    public CompilationUnit parse(String input) {
        final CharStream charStream = CharStreams.fromString(input);

        final FunLexer lexer = new FunLexer(charStream);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final FunParser funParser = new FunParser(tokens);
        return (CompilationUnit) astBuilder.visitCompilationUnit(funParser.compilationUnit());
    }

}
