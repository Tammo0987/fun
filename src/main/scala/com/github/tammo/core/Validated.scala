package com.github.tammo.core

sealed trait Validated[+E, +A] {

  def map[B](f: A => B): Validated[E, B] = this match
    case Valid(value)        => Valid(f(value))
    case invalid: Invalid[E] => invalid

  def flatMap[EE >: E, B](f: A => Validated[EE, B]): Validated[EE, B] =
    this match
      case Valid(value)        => f(value)
      case invalid: Invalid[E] => invalid

  def combine[EE >: E, B, C](
      that: Validated[EE, B]
  )(f: (A, B) => C)(using Semigroup[EE]): Validated[EE, C] =
    (this, that) match
      case (Valid(a), Valid(b))       => Valid(f(a, b))
      case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[EE].combine(e1, e2))
      case (Invalid(e), _)            => Invalid(e)
      case (_, Invalid(e))            => Invalid(e)

}

object Validated {
  def valid[A](value: A): Validated[Nothing, A] = Valid(value)
  def invalid[E](error: E): Validated[E, Nothing] = Invalid(error)
}

final case class Valid[+A](value: A) extends Validated[Nothing, A]
final case class Invalid[+E](errors: E) extends Validated[E, Nothing]
