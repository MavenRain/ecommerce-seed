package com.example.client

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.web.html.h1
import zio.prelude.fx.ZPure

@react
object Scratchpad {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    h1(
      "From scratchpad: " +
        ZPure
          .succeed(5)
          .asState(2)
          .flatMap(x => ZPure.succeed(x * 3).asState(4))
          .run(5)
          .toString
    )
  }
}