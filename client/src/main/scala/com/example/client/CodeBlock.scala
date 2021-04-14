package com.example.client

import scala.scalajs.js.{Dictionary, Object}
import scala.scalajs.js.Dynamic.literal
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.web.html.{div, style}

@react
object CodeBlock {
  type Props = String
  private val prismColors = Dictionary[Object](
    "hljs-comment" -> literal(color = "#999999"),
    "hljs-keyword" -> literal(color = "#c5a5c5"),
    "hljs-built_in" -> literal(color = "#5a9bcf"),
    "hljs-string" -> literal(color = "#8dc891"),
    "hljs-variable" -> literal(color = "#d7deea"),
    "hljs-title" -> literal(color = "#79b6f2"),
    "hljs-type" -> literal(color = "#FAC863"),
    "hljs-meta" -> literal(color = "#FAC863"),
    "hljs-strong" -> literal(fontWeight = 700),
    "hljs-emphasis" -> literal(fontStyle = "italic"),
    "hljs" -> literal(
      backgroundColor = "#282c34",
      color = "#ffffff",
      fontSize = "15px",
      lineHeight = "20px"
    ),
    "code[class*=\"language-\"]" -> literal(
      backgroundColor = "#282c34",
      color = "#ffffff"
    )
  )
  val component = FunctionalComponent[Props] { props =>
    div(style := literal(
      width = "100%",
      display = "block",
      padding = "10px",
      backgroundColor = "#282c34",
      boxSizing = "border-box",
      height = "calc(100% - 36px)",
      overflow = "auto"
    ))(
      SyntaxHighlighter(language = "scala", style = prismColors)(
        props
      )
    )
  }
}