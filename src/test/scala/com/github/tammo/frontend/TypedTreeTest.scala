package com.github.tammo.frontend

import com.github.tammo.diagnostics.PositionSpan
import com.github.tammo.frontend.`type`.TypedTree.*
import com.github.tammo.frontend.`type`.Type
import org.scalactic.source.Position
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should

class TypedTreeTest extends AnyFlatSpecLike with should.Matchers {

  "FunctionApplication".should("return the correct type") in {
    val span = PositionSpan("", 0, 0)
    val functionApplication = FunctionApplication(
      TypedIdentifier("test", Type.Unit, span),
      Seq(IntLiteral(1, span), StringLiteral("Test", span)),
      span
    )

    functionApplication.`type`.shouldBe(
      Type.FunctionType(
        Type.Int,
        Type.FunctionType(Type.String, Type.Unit)
      )
    )
  }

}
