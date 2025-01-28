package com.github.tammo.fun.backend.codegen;

import com.github.tammo.fun.frontend.type.TypedTreeNode.CompilationUnit;

public interface CodeGenerator {

    byte[] generate(CompilationUnit programm);

}
