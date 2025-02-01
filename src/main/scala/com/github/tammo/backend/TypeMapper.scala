package com.github.tammo.backend

import com.github.tammo.frontend.`type`.Type
import com.github.tammo.frontend.`type`.Type.*

object TypeMapper {

  def mapTypeToBytecodeString(`type`: Type): String = {
    `type` match {
      case Unit        => "V"
      case Int         => "I"
      case Boolean     => "B"
      case String      => "Ljava/lang/String;"
      case StringArray => "[Ljava/lang/String;"
      case functionType: FunctionType =>
        mapTypeAsFunctionTypeToBytecodeString(functionType)
      case _ => throw new IllegalStateException(s"Unexpected type mapping for ${`type`}")
    }
  }

  def mapTypeAsFunctionTypeToBytecodeString(`type`: Type): String =
    `type` match {
      case functionType: FunctionType => mapFunctionType(functionType)
      case _                          => s"()${mapTypeToBytecodeString(`type`)}"
    }

  private def mapFunctionType(functionType: FunctionType): String =
    s"(${mapFunctionTypeParameter(functionType)})${mapTypeToBytecodeString(functionType.returnType)}"

  private def mapFunctionTypeParameter(
      functionType: Type.FunctionType
  ): String =
    functionType.parameter match
      case parameter: FunctionType =>
        mapFunctionTypeParameter(parameter) + mapTypeToBytecodeString(
          functionType.returnType
        )
      case _ => mapTypeToBytecodeString(functionType.parameter)

}
