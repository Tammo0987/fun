package com.github.tammo.fun.backend.codegen;

import com.github.tammo.fun.frontend.ast.SyntaxNode.CompilationUnit;

public interface CodeGenerator {

    byte[] generate(CompilationUnit programm);

}
