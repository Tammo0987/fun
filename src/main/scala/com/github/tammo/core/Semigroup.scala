package com.github.tammo.core

trait Semigroup[A]:
  def combine(a1: A, a2: A): A

object Semigroup:

  def apply[A](using semigroup: Semigroup[A]): Semigroup[A] = semigroup
