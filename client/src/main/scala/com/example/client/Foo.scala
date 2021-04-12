package com.example.client

import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.ext.Ajax.get
import scala.util.chaining.scalaUtilChainingOps
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.Fragment
import slinky.web.html.h1
import zio.{Runtime, ZIO}
//https://swapi.dev/api/people/
@react
object Foo {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    useState("").pipe { case (text, setText) =>
      useEffect(() =>
        Runtime.default.unsafeRunAsync_(ZIO.fromFuture(context => get("http://localhost:11111/").map(_.responseText)(context)).fold(
          { case ex: AjaxException => setText(ex.toString) },
          setText(_)
        ))
      ).pipe(_ =>
        Fragment(
          h1("Goodbye world!"),
          h1(text)
        )
      )
    }
  }
}