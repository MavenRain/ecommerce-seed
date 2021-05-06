package io.github.mavenrain.persistence

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scala.util.chaining.scalaUtilChainingOps
import zio.{IO, Runtime}
trait RepositoryLaws  {
  this: AnyFlatSpec with Matchers with RepositoryProvider with ScalaCheckDrivenPropertyChecks =>
  type Error = String
  type Item = (Int, String, Boolean)
  type Id = String
  type RepositoryCreator = Seq[Item] => Repository[String, Item]
  val createRepository: RepositoryCreator
  
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