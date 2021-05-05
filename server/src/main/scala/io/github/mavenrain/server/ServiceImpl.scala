package io.github.mavenrain.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.github.mavenrain.{
  ProductApi, ReadProductRequest, ReadProductResponse
}
import io.github.mavenrain.domain.repositories.ProductRepository.{
  RichResponse, RichString
}
import scala.util.chaining.scalaUtilChainingOps

trait ServiceImpl extends ProductApi {
  def readProduct(request: ReadProductRequest): Source[ReadProductResponse, NotUsed] =
    request
      .identifiers
      .pipe(Source(_))
      .map(_.toDomain)
      .map(_.toContractResponse)
}

object ServiceImpl {
  def apply(): ServiceImpl = new ServiceImpl {}
}

import shapeless.{::, :+:, CNil, Coproduct, Generic, HNil, Poly1, Poly2}
import shapeless.syntax.inject.InjectSyntax

object PlayGround {
  case class MigrateFrom(a: String, b: Int, c: Char)
  case class MigrateTo(a: Int, b: Char, c: String)
  def migration(x: MigrateFrom) =
    Generic[MigrateFrom]
      .to(x)
      .intersect[Int :: Char :: String :: HNil]
      .align[Int :: Char :: String :: HNil]
      .pipe(Generic[MigrateTo].from(_))
  type P1 = Int :+: String :+: Boolean :+: CNil
  type P2 = Char :+: Boolean :+: Unit :+: CNil
  type P3 = Int :+: String :+: CNil
  type P4 = Boolean :+: Unit :+: CNil
  object Juxtapose extends Poly1 {
    implicit val integer = at[Int] { _ => Coproduct[P2](()) }
    implicit val string = at[String] { _ => Coproduct[P2](()) }
    implicit val boolean = at[Boolean] { Coproduct[P2](_) }
  }
  object Evaluate extends Poly2 {
    implicit val one = at[Int, Boolean] { (_, _) => 1 }
    implicit val two = at[Int, Unit] { (_, _) => 2 }
    implicit val three = at[String, Boolean] { (_, _) => 3 }
    implicit val four = at[String, Unit] { (_, _) => 4 }
  }
  val x = Coproduct[P1](1) flatMap Juxtapose
  val y = "Hello world!".inject[P1]
  val yy = true.inject[P2]
  val aa = y.zipWith('c' :: true :: () :: HNil)
  val z = Evaluate("f", ())

}