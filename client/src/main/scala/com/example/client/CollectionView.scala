package com.example.client

import scala.util.chaining.scalaUtilChainingOps
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{key, ul, li}

@react
object CollectionView {
  type Props = Seq[ReactElement]
  val component = FunctionalComponent[Props] {
    _
      .zipWithIndex
      .collect { case (element, index) =>
        li(key := index.toString)(element)
      }
      .pipe(ul(_))
  }
}