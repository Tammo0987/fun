package com.github.tammo.frontend

import com.github.tammo.frontend.`type`.TypedTree.*
import com.github.tammo.frontend.`type`.Type

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should

class TypedTreeTest extends AnyFlatSpecLike with should.Matchers {

  "FunctionApplication".should("return the correct type") in {
    val functionApplication = FunctionApplication(
      TypedIdentifier("test", Type.Unit),
      Seq(IntLiteral(1), StringLiteral("Test"))
    )

    functionApplication.`type`.shouldBe(
      Type.FunctionType(
        Type.Int,
        Type.FunctionType(Type.String, Type.Unit)
      )
    )
  }

}
