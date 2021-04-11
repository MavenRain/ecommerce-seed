package com.example.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.pattern.after
import akka.stream.scaladsl.{Sink, Source}
import com.example.{Request, Response, Service}
import scala.concurrent.Future
import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.util.chaining.scalaUtilChainingOps

trait ServiceImpl extends Service {

  protected implicit val actorSystem: ActorSystem[_]

  override def unary(in: Request): Future[Response] =
    after(2.seconds)(Future.successful(Response(s"Received [${in.payload}]")))

  override def serverStreaming(in: Request): Source[Response, NotUsed] =
    Source
      .repeat(in)
      .zipWithIndex
      .collect {
        case (in, idx) =>
          Response(s"Received [${in.payload}] idx [$idx]")
      }
      .throttle(1, 0.5.seconds)
      .take(20)

  override def bidiStreaming(in: Source[Request, NotUsed]): Source[Response, NotUsed] =
    in.map(in => Response(s"Received [${in.payload}]")).throttle(1, 0.5.seconds)

  override def clientStreaming(in: Source[Request, NotUsed]): Future[Response] =
    in
      .zipWithIndex
      .collect { case (in, idx) => (in, idx) }
      .runWith(Sink.lastOption)
      .map(_.fold(s"Received nothing") { case (lastIn, lastIdx) =>
        s"Received last [$lastIn] idx [$lastIdx]"
      })(actorSystem.executionContext)
      .map(Response(_))(actorSystem.executionContext)
}
