package com.github.tammo.frontend.`type`

import com.github.tammo.diagnostics.CompilerError.{CyclicTypeReferenceError, IncompatibleTypesError, TypeCheckError}
import com.github.tammo.diagnostics.PositionSpan

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
  ): Either[Seq[TypeCheckError], Substitutions] = {
    if (constraints.isEmpty) {
      Right(Map.empty)
    } else {
      constraints.foldLeft[Either[Seq[TypeCheckError], Substitutions]](
        Right(Map.empty[Type.Variable, Type])
      ) {
        case (Right(substitutions), constraint) =>
          val result = unify(
            constraint.left,
            constraint.right,
            constraint.span,
            substitutions
          )
          result match {
            case right: Right[Seq[TypeCheckError], Substitutions] =>
              right.map(substitutions ++ _)
            case typeErrors: Left[Seq[TypeCheckError], Substitutions] =>
              typeErrors
          }
        case (typeErrors: Left[Seq[TypeCheckError], Substitutions], _) =>
          typeErrors
      }
    }
  }

  private def unify(
      left: Type,
      right: Type,
      positionSpan: PositionSpan,
      substitutions: Substitutions
  ): Either[Seq[TypeCheckError], Substitutions] = {
    val substitutedLeft = applySubstitution(substitutions, left)
    val substitutedRight = applySubstitution(substitutions, right)

    if (substitutedLeft == substitutedRight) {
      Right(substitutions)
    } else {
      (substitutedLeft, substitutedRight) match {
        case (leftTypeVariable: Type.Variable, _) =>
          unifyVariable(
            leftTypeVariable,
            substitutedRight,
            positionSpan,
            substitutions
          )
        case (_, rightTypeVariable: Type.Variable) =>
          unifyVariable(
            rightTypeVariable,
            substitutedLeft,
            positionSpan,
            substitutions
          )
        case (
              Type.FunctionType(leftParameter, leftReturnType),
              Type.FunctionType(rightParameter, rightReturnType)
            ) =>
          val unifiedParameter =
            unify(leftParameter, rightParameter, positionSpan, substitutions)
          val unifiedReturnType =
            unify(leftReturnType, rightReturnType, positionSpan, substitutions)
          (unifiedParameter, unifiedReturnType) match {
            case (Right(parameter), Right(returnType)) =>
              Right(parameter ++ returnType)
            case (
                  typeErrors: Left[Seq[TypeCheckError], Substitutions],
                  otherTypeErrors: Left[Seq[TypeCheckError], Substitutions]
                ) =>
              Left(typeErrors.value ++ otherTypeErrors.value)
            case (typeErrors: Left[Seq[TypeCheckError], Substitutions], _) =>
              typeErrors
            case (_, typeErrors: Left[Seq[TypeCheckError], Substitutions]) =>
              typeErrors
          }
        case _ =>
          Left(
            Seq(
              IncompatibleTypesError(
                substitutedLeft,
                substitutedRight,
                positionSpan
              )
            )
          )
      }
    }
  }

  private def unifyVariable(
      variable: Type.Variable,
      other: Type,
      positionSpan: PositionSpan,
      substitutions: Substitutions
  ): Either[Seq[TypeCheckError], Substitutions] = {
    val currentSubstitutions = substitutions.get(variable)

    currentSubstitutions match
      case Some(currentSubstitution) =>
        unify(currentSubstitution, other, positionSpan, substitutions)

      case None =>
        other match
          case otherVariable: Type.Variable =>
            val maybeSubstitutions = substitutions
              .get(otherVariable)
              .map(unifyVariable(variable, _, positionSpan, substitutions))

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
  ): Either[Seq[TypeCheckError], Substitutions] = {
    if (occursCheck(variable, other)) {
      Left(Seq(CyclicTypeReferenceError(variable, other)))
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
