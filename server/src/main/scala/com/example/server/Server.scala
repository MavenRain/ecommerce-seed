package com.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.scaladsl.WebHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.{Directives, Route, RouteResult}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.example.{BuildInfo, ServiceHandler}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory.getLogger
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.chaining.scalaUtilChainingOps
import zio.{Runtime, ZIO}

object Server extends Directives {

  private val logger = getLogger(getClass)

  private implicit val corsSettings: CorsSettings =
    if (BuildInfo.environmentMode.equalsIgnoreCase("development"))
      CorsSettings.defaultSettings
    else
      WebHandler.defaultCorsSettings

  private def startHttpServer(port: Int)(implicit actorSystem: ActorSystem[_]): Unit =
    Runtime.default.unsafeRunAsync_(ZIO.fromFuture(executionContext =>
      WebHandler
        .grpcWebHandler(ServiceHandler.partial(
            actorSystem.pipe(system => new ServiceImpl { val actorSystem = system })
        ))
        .pipe(grpcWebServiceHandlers =>
          Http()
            .newServerAt(
              interface = "0.0.0.0",
              port = port
            )
            .bind(Route.toFunction(concat(
              new WebService().route,
              ctx => grpcWebServiceHandlers(ctx.request).map(RouteResult.Complete)(executionContext)
            )))
        )
    ).fold(
      ex => logger.error(s"gRPC server binding failed", ex).tap(_ => actorSystem.terminate()),
      binding => logger.info(s"gRPC server bound to: ${binding.localAddress}")
    ))

  def main(args: Array[String]): Unit =
    ActorSystem[Nothing](
      Behaviors.setup[Nothing] { context =>
        startHttpServer(9000)(context.system)
        Behaviors.empty
      },
      "ecommerce-seed",
      ConfigFactory
        .parseString("akka.http.server.preview.enable-http2 = on")
        .withFallback(ConfigFactory.defaultApplication())
    )
}
