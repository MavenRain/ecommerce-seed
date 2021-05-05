package com.example.client

import scala.scalajs.js.Dynamic.literal
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.web.html.{b, div, style}
import shapeless.{::, HNil}
import zio.prelude.Newtype

@react
object LineItem {
  object Title extends Newtype[String]
  type Title = Title.Type
  object Text extends Newtype[String]
  type Text = Text.Type
  type Props = Title :: Text :: HNil
  val component = FunctionalComponent[Props] { case props =>
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
        b(style := literal(color = "rgb(109, 109, 109)"))(Title.unwrap(props.select[Title]))
      ),
      div(style := literal(
        overflow = "auto",
        padding = "10px",
        boxSizing = "border-box"
      ))(Text.unwrap(props.select[Text]))
    ) 
  }
}