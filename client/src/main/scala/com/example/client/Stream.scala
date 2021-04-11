package com.example.client

import com.example.client.App.serviceStub
import com.example.service.Request
import com.example.service.Response
import io.grpc.stub.StreamObserver
import scala.scalajs.js.timers.{clearTimeout, setTimeout}
import scala.util.chaining.scalaUtilChainingOps
import scalapb.grpcweb.Metadata
import shapeless.{::, HNil}
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.web.html.{div, h2, p}

@react
object Stream {
  case class Props(cancel: Boolean)

  val component = FunctionalComponent[Props] { props =>
    useState("Request pending" :: 0 :: HNil).pipe { case (status :: count :: HNil, setStatus) =>
      useEffect(
        () => {
          serviceStub
            .serverStreaming(
              Request(payload = "Hello!"),
              Metadata("custom-header-2" -> "streaming-value"),
              new StreamObserver[Response] {
                override def onNext(value: Response): Unit =
                  setStatus({ case _ :: count :: HNil => s"Received success [$count]" :: count + 1 :: HNil })

                override def onError(ex: Throwable): Unit =
                  setStatus({ case _ :: count :: HNil => s"Received failure: $ex" :: count :: HNil })

                override def onCompleted(): Unit =
                  setStatus({ case _ :: count :: HNil  => s"Received completed" :: count :: HNil })
              }
            )
            .pipe(stream =>
              setStatus("Request sent" :: count :: HNil)
                .pipe(_ =>
                  if (props.cancel)
                    Some(setTimeout(5000) {
                      setStatus({ case _ :: count :: HNil  => s"Stream stopped by client" :: count :: HNil })
                      stream.cancel()
                    })
                  else
                    None
                )
                .pipe(maybeTimer =>
                  () => stream.cancel().tap(_ => maybeTimer.foreach(clearTimeout))
                )
            )
        },
        Seq.empty
      ).pipe(_ =>
        div(
          h2("Stream request:"),
          p(status)
        )
      )
    }
  }
}
