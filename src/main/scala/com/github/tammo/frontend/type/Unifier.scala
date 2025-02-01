package com.github.tammo.frontend.`type`

object Unifier {

  private type Substitutions = Map[Type.Variable, Type]

  def applySubstitution(
      substitutions: Substitutions,
      `type`: Type
  ): Type = `type` match
    case typeVariable: Type.Variable =>
      substitutions
        .get(typeVariable)
        .map(applySubstitution(substitutions, _))
        .getOrElse(typeVariable)
    case Type.FunctionType(parameter, returnType) =>
      Type.FunctionType(
        applySubstitution(substitutions, parameter),
        applySubstitution(substitutions, returnType)
      )
    case _ => `type`

  def unifyAll(constraints: Seq[Constraint]): Substitutions = {
    if (constraints.isEmpty) {
      Map.empty
    } else {
      constraints.foldLeft(Map.empty[Type.Variable, Type]) {
        (substitutions, constraint) =>
          substitutions ++ unify(
            constraint.left,
            constraint.right,
            substitutions
          )
      }
    }
  }

  private def unify(
      left: Type,
      right: Type,
      substitutions: Substitutions
  ): Substitutions = {
    val substitutedLeft = applySubstitution(substitutions, left)
    val substitutedRight = applySubstitution(substitutions, right)

    if (substitutedLeft == substitutedRight) {
      substitutions
    } else {
      (substitutedLeft, substitutedRight) match {
        case (leftTypeVariable: Type.Variable, _) =>
          unifyVariable(leftTypeVariable, substitutedRight, substitutions)
        case (_, rightTypeVariable: Type.Variable) =>
          unifyVariable(rightTypeVariable, substitutedLeft, substitutions)
        case (
              Type.FunctionType(leftParameter, leftReturnType),
              Type.FunctionType(rightParameter, rightReturnType)
            ) =>
          unify(leftParameter, rightParameter, substitutions) ++ unify(
            leftReturnType,
            rightReturnType,
            substitutions
          )
        case _ =>
          throw new IllegalStateException(s"Cannot unify $left with $right")
      }
    }
  }

  private def unifyVariable(
      variable: Type.Variable,
      other: Type,
      substitutions: Substitutions
  ): Substitutions = {
    val currentSubstitutions = substitutions.get(variable)

    currentSubstitutions match
      case Some(currentSubstitution) =>
        unify(currentSubstitution, other, substitutions)

      case None =>
        other match
          case otherVariable: Type.Variable =>
            val maybeSubstitutions = substitutions
              .get(otherVariable)
              .map(unifyVariable(variable, _, substitutions))

            maybeSubstitutions match {
              case Some(substitutions) => substitutions
              case _ => addSubstitution(variable, other, substitutions)
            }
          case _ => addSubstitution(variable, other, substitutions)
  }

  private def addSubstitution(
      variable: Type.Variable,
      other: Type,
      substitutions: Substitutions
  ): Substitutions = {
    if (occursCheck(variable, other)) {
      throw new IllegalStateException(
        s"Type variable $variable occurs in $other"
      )
    }

    substitutions + (variable -> other)
  }

  private def occursCheck(variable: Type.Variable, other: Type): Boolean = {
    other match
      case otherVariable: Type.Variable => otherVariable == variable
      case functionType: Type.FunctionType =>
        occursCheck(variable, functionType.parameter) || occursCheck(
          variable,
          functionType.returnType
        )
      case _ => false
  }

}
