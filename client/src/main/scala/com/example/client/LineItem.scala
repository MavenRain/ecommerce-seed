package com.example.client

import scala.scalajs.js.Dynamic.literal
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.web.html.{b, div, style}

@react
object LineItem {
  type Props = String
  val component = FunctionalComponent[Props] { props =>
    div(style := literal(
      width = "35%",
      boxSizing = "border-box",
      borderLeft = "1px solid rgb(236, 236, 236)"
    ))(
      div(style := literal(
        backgroundColor = "rgb(236, 236, 236)",
        padding = "10px",
        boxSizing = "border-box"
      ))(
        b(style := literal(color = "rgb(109, 109, 109)"))("RESULT")
      ),
      div(style := literal(
        overflow = "auto",
        padding = "10px",
        boxSizing = "border-box"
      ))(props)
    ) 
  }
}