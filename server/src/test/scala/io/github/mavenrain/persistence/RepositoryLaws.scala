package io.github.mavenrain.persistence

import org.scalacheck.Arbitrary
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scala.util.chaining.scalaUtilChainingOps
import zio.{IO, Runtime}

trait RepositoryLaws  {
  this: AnyFlatSpec with Matchers with RepositoryProvider with ScalaCheckDrivenPropertyChecks =>
  type RepositoryCreator = Seq[Item] => Repository
  val createRepository: RepositoryCreator
  protected implicit val arbitraryItem: Arbitrary[Item]
  protected implicit val arbitraryId: Arbitrary[Hash]
  
  it should "read an existing item" in {
    forAll { (item: Item, hash: Hash) =>
      Runtime.default.unsafeRun(
        createRepository(Seq(item))
          .select[Reader]
          .pipe(_(Seq(hash)))
          .flatMap(_.headOption.flatMap(_.select[Item]).pipe(IO.fromOption(_)))
          .map(_ shouldBe item)
      )
    }
  }

  it should "create a new item" in {
    forAll { (item: Item, hash: Hash) =>
      Runtime.default.unsafeRun(
        createRepository(Nil)
          .pipe(repository =>
            repository
              .select[Creator]
              .pipe(_(Seq(item)))
              .flatMap(_ => repository.select[Reader].pipe(_(Seq(hash))))
              .map(_ shouldBe item)
          )
      )
    }
  }
}