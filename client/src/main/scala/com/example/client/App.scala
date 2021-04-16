package com.example.client

import com.example.service.ServiceGrpcWeb
import scala.scalajs.LinkingInfo
import scalapb.grpc.Channels
import scalapb.grpcweb.Metadata
import shapeless.HNil
import slinky.core.{StatelessComponent, Tag}
import slinky.core.annotations.react
import slinky.core.facade.{Fragment, ReactElement}
import slinky.web.html.h1

@react
class App extends StatelessComponent {
  type Props = Unit

  def render(): ReactElement =
    Fragment(
      h1("Hello world!"),
      Unary(),
      Stream(cancel = false),
      Stream(cancel = true),
      Scratchpad(),
      CollectionView(Seq(
        LineItem(LineItem.Title("Line Item") :: LineItem.Text("first") :: HNil),
        LineItem(LineItem.Title("Line Item") :: LineItem.Text("second") :: HNil),
        LineItem(LineItem.Title("Line Item") :: LineItem.Text("third") :: HNil)
      )),
      Button()
    )
}

object App {

  def createTag[T <: Tag](tag: T): ReactElement =
    tag.apply("Hello")

  val serviceStub: ServiceGrpcWeb.Service[Metadata] =
    ServiceGrpcWeb.stub(
      Channels.grpcwebChannel(
        if (LinkingInfo.developmentMode)
          "http://localhost:9000"
        else
          ""
      )
    )
}
