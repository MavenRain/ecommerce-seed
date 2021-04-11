package com.example.client

import org.scalajs.dom
import slinky.hot
import slinky.web.ReactDOM.render
import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.chaining.scalaUtilChainingOps

object Main {

  @JSExportTopLevel("main")
  def main(): Unit = {
    if (developmentMode)
      hot.initialize()
   Option(dom.document.getElementById("root")).getOrElse {
        val elem = dom.document.createElement("div")
        elem.id = "root"
        dom.document.body.appendChild(elem)
        elem
      }
      .pipe(container => render(App(), container))
  }
}
