package com.github.tammo.frontend.`type`

sealed trait Type

object Type {

  def fromString(name: String): Type = name match
    case "Int" => Int
    case "Boolean" => Boolean
    case "String" => String
    case "String[]" => StringArray
    case "Unit" => Unit
    case _ => Variable()

  sealed trait ResolvedType extends Type

  sealed trait PrimitiveType extends ResolvedType

  case object Int extends PrimitiveType

  case object String extends PrimitiveType

  case object Boolean extends PrimitiveType

  case object Unit extends PrimitiveType // TODO extends ????

  case object StringArray extends PrimitiveType

  case class FunctionType(parameter: Type, returnType: Type)
      extends ResolvedType

  case class Variable() extends Type
}
