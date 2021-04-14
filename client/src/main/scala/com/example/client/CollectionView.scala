package com.example.client

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.ul

@react
object CollectionView {
  type Props = Seq[ReactElement]
  val component = FunctionalComponent[Props] { ul(_) }
}