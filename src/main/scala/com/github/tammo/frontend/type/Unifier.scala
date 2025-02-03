package com.github.tammo.frontend.`type`

import com.github.tammo.diagnostics.CompilerError.{CyclicTypeReferenceError, TypeCheckError}

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

  def unifyAll(
      constraints: Seq[Constraint]
  ): Either[TypeCheckError, Substitutions] = {
    if (constraints.isEmpty) {
      Right(Map.empty)
    } else {
      constraints.foldLeft[Either[TypeCheckError, Substitutions]](
        Right(Map.empty[Type.Variable, Type])
      ) {
        case (Right(substitutions), constraint) =>
          val result = unify(constraint.left, constraint.right, substitutions)
          result match {
            case right: Right[TypeCheckError, Substitutions] =>
              right.map(substitutions ++ _)
            case typeError: Left[TypeCheckError, Substitutions] => typeError
          }
        case (typeError: Left[TypeCheckError, Substitutions], _) => typeError
      }
    }
  }

  private def unify(
      left: Type,
      right: Type,
      substitutions: Substitutions
  ): Either[TypeCheckError, Substitutions] = {
    val substitutedLeft = applySubstitution(substitutions, left)
    val substitutedRight = applySubstitution(substitutions, right)

    if (substitutedLeft == substitutedRight) {
      Right(substitutions)
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
          val unifiedParameter =
            unify(leftParameter, rightParameter, substitutions)
          val unifiedReturnType =
            unify(leftReturnType, rightReturnType, substitutions)
          (unifiedParameter, unifiedReturnType) match {
            case (Right(parameter), Right(returnType)) =>
              Right(parameter ++ returnType)
            case (typeError: Left[TypeCheckError, Substitutions], _) =>
              typeError
            case (_, typeError: Left[TypeCheckError, Substitutions]) =>
              typeError
          }
        case _ =>
          throw new IllegalStateException(s"Cannot unify $left with $right")
      }
    }
  }

  private def unifyVariable(
      variable: Type.Variable,
      other: Type,
      substitutions: Substitutions
  ): Either[TypeCheckError, Substitutions] = {
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
  ): Either[TypeCheckError, Substitutions] = {
    if (occursCheck(variable, other)) {
      Left(CyclicTypeReferenceError(variable, other))
    } else {
      Right(substitutions + (variable -> other))
    }
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
