package com.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.scaladsl.WebHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route, RouteResult}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import io.github.mavenrain.ProductApiHandler
import io.github.mavenrain.persistence.Transactions.{initializeSession, kickTheTires}
import io.github.mavenrain.server.{ServiceImpl => EcomServer}
import com.example.{BuildInfo, ServiceHandler}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory.getLogger
import scala.util.chaining.scalaUtilChainingOps
import shapeless.{::, HNil}
import zio.{Runtime, ZIO}
import zio.prelude.Newtype

object Server extends Directives {

  private val logger = getLogger(getClass)

  private implicit val corsSettings: CorsSettings =
    if (BuildInfo.environmentMode.equalsIgnoreCase("development"))
      CorsSettings.defaultSettings
    else
      WebHandler.defaultCorsSettings
  
  private object ServicePort extends Newtype[Int]
  private type ServicePort = ServicePort.Type
  private object ProductApiPort extends Newtype[Int]
  private type ProductApiPort = ProductApiPort.Type

  private def startHttpServer(ports: ServicePort :: ProductApiPort :: HNil)(implicit actorSystem: ActorSystem[_]): Unit =
    Runtime.default.unsafeRunAsync_(
      ZIO.collectAllPar(Seq(
        ZIO.fromFuture(executionContext =>
          WebHandler
            .grpcWebHandler(ServiceHandler.partial(
                actorSystem.pipe(system => new ServiceImpl { val actorSystem = system })
            ))
            .pipe(grpcWebServiceHandlers =>
              Http()
                .newServerAt(
                  interface = "0.0.0.0",
                  port = ServicePort.unwrap(ports.select[ServicePort])
                )
                .bind(Route.toFunction(concat(
                  WebService().route,
                  ctx => grpcWebServiceHandlers(ctx.request).map(RouteResult.Complete)(executionContext)
                )))
            )
        ).fold(
          ex => logger.error(s"gRPC server binding failed", ex).tap(_ => actorSystem.terminate()),
          binding => logger.info(s"gRPC server bound to: ${binding.localAddress}")
        ),
        ZIO.fromFuture(executionContext =>
          WebHandler.grpcWebHandler(ProductApiHandler.partial(
            EcomServer()
          ))
          .pipe(grpcWebServiceHandlers =>
            Http()
              .newServerAt(
                interface = "0.0.0.0",
                port = ProductApiPort.unwrap(ports.select[ProductApiPort])
              )
              .bind(Route.toFunction(concat(
                WebService().route,
                ctx => grpcWebServiceHandlers(ctx.request).map(RouteResult.Complete)(executionContext)
              )))
          )
        ).fold(
          ex => logger.error(s"gRPC server binding failed", ex).tap(_ => actorSystem.terminate()),
          binding => logger.info(s"gRPC server bound to: ${binding.localAddress}")
        )
      ))
    )

  def main(args: Array[String]): Unit =
    ActorSystem[Nothing](
      Behaviors.setup[Nothing] { context =>
        startHttpServer(ServicePort(9000) :: ProductApiPort(12000) :: HNil)(context.system)
        Behaviors.empty
      },
      "ecommerce-seed",
      ConfigFactory
        .parseString("akka.http.server.preview.enable-http2 = on")
        .withFallback(ConfigFactory.defaultApplication())
    )
    .tap(_ => initializeSession)
    .tap(_ => kickTheTires)
}
