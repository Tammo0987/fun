package com.github.tammo.fun.gen;

import com.github.tammo.fun.ast.SyntaxNode.CompilationUnit;

public interface CodeGenerator {

    byte[] generate(CompilationUnit programm);

}
