package io.github.mavenrain.persistence

import org.scalacheck.Arbitrary
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scala.util.chaining.scalaUtilChainingOps
import zio.{IO, Runtime}

trait RepositoryLaws  {
  this: AnyFlatSpec with Matchers with RepositoryProvider with ScalaCheckDrivenPropertyChecks =>
  type Item
  type Id
  type RepositoryCreator = Seq[Item] => Repository[Id, Item]
  val createRepository: RepositoryCreator
  protected implicit val arbitraryItem: Arbitrary[Item]
  protected implicit val arbitraryId: Arbitrary[Id]
  
  forAll { (item: Item, id: Id) =>
    Runtime.default.unsafeRun(
      createRepository(Seq(item))
        .select[Reader[Id, Item]]
        .pipe(_(id))
        .flatMap(_.select[Item].pipe(IO.fromOption(_)))
        .map(_ shouldBe item)
    )
  }
}