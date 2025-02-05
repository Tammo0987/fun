package com.github.tammo.core

import com.github.tammo.diagnostics.CompilerError

trait Phase[I, O]:

  def run(input: I): Validated[List[CompilerError], O]

  def map[P](f: O => P): Phase[I, P] = (input: I) =>
    Phase.this.run(input).map(f)

  def flatMap[P](f: O => Phase[I, P]): Phase[I, P] = (input: I) =>
    Phase.this.run(input) match
      case Valid(value)                          => f(value).run(input)
      case invalid: Invalid[List[CompilerError]] => invalid

  def andThen[P](next: Phase[O, P]): Phase[I, P] = new Phase[I, P] {
    override def run(input: I): Validated[List[CompilerError], P] =
      Phase.this.run(input) match
        case Valid(value)                          => next.run(value)
        case invalid: Invalid[List[CompilerError]] => invalid
  }

object Phase:
  def apply[I, O](f: I => Validated[List[CompilerError], O]): Phase[I, O] =
    (input: I) => f(input)
