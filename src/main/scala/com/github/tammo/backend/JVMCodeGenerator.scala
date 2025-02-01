package com.github.tammo.backend

import com.github.tammo.frontend.`type`.TypedTree.CompilationUnit
import com.github.tammo.frontend.`type`.{TypedTree, TypedTreeVisitor}
import org.objectweb.asm.ClassWriter

object JVMCodeGenerator extends CodeGenerator {

  override def generate(compilationUnit: CompilationUnit): Array[Byte] = {
    val classWriter = ClassWriter(
      ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
    )
    val typedTreeWriter =
      TypedTreeWriter(classWriter, compilationUnit.fullyQualifiedName)
    TypedTreeVisitor.visit(compilationUnit, typedTreeWriter)
    typedTreeWriter.toByteCode
  }

}
