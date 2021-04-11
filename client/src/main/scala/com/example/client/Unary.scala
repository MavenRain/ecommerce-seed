package com.example.client

import com.example.client.App.serviceStub
import com.example.service.Request
import scala.util.chaining.scalaUtilChainingOps
import scalapb.grpcweb.Metadata
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.web.html.{div, h2, p}
import zio.{Runtime, ZIO}

@react
object Unary {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    useState("Request pending").pipe { case (status, setStatus) =>
      useEffect(
        () =>
          Runtime
            .default
            .unsafeRunAsync_(
              ZIO.fromFuture(_ => serviceStub.unary(Request(payload = "Hello!"), Metadata("custom-header-1" -> "unary-value")))
                .fold(
                  ex => setStatus(s"Request failure: $ex"),
                  value => setStatus(s"Request success: ${value.payload}")
                )
            )
            .tap(_ => setStatus("Request sent")),
        Seq.empty
      ).pipe(_ =>
        div(
          h2("Unary request:"),
          p(status)
        )
      )
    }
  }
}
