package com.example.client

import scala.scalajs.js.Dynamic.literal
import scala.util.chaining.scalaUtilChainingOps
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.useState
import slinky.web.html.{button, div, h1, onClick, style}

@react
object Button {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    useState(false).pipe { case (on, set) =>
      div(style := literal(
        position = "relative",
        fontSize = "14px",
        fontWeight = "600",
        textAlign = "center",
        padding = "0.7em 1.2em",
        cursor = "pointer",
        userSelect = "none",
        display = "inline-flex",
        alignItems = "center",
        justifyContent = "center",
        height = "32px",
        minWidth = "96px",
        borderRadius = "4px",
        backgroundColor = "#fff",
        border = "1px solid",
        color = "#fff"
      ))(
        button(
          onClick := (_ => set(!on)),
          div(h1(on.toString))
        )
      )
    }
  }
}