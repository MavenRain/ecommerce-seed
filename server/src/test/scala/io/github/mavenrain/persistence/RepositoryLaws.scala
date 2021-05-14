package io.github.mavenrain.persistence

import org.scalacheck.Arbitrary
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scala.util.chaining.scalaUtilChainingOps
import zio.{IO, Runtime}

trait RepositoryLaws  {
  this: AnyFlatSpec with Matchers with RepositoryProvider with ScalaCheckDrivenPropertyChecks =>
  type RepositoryCreator = (Seq[Item], Seq[Hash]) => Repository
  protected val createRepository: RepositoryCreator
  protected implicit val arbitraryItem: Arbitrary[Item]
  protected implicit val arbitraryId: Arbitrary[Hash]
  protected def expectedItems(items: Seq[Item], hashes: Seq[Hash]): Seq[Item]
  protected def createSchema: Unit
  protected def hashFromItem(item: Item): Hash

  Transactions.initializeSession()
  createSchema

  it should "read an existing item" in {
    forAll { (item: Item, hash: Hash) =>
      Runtime.default.unsafeRun(
        createRepository(Seq(item), Seq(hash))
          .pipe(repository =>
            repository
              .select[Reader]
              .pipe(_(Seq(hash)))
              .flatMap(_.select[Seq[Item]].pipe(IO.fromOption(_)))
              .map(_ shouldBe expectedItems(Seq(item), Seq(hash)))
              .map(_ =>
                repository
                  .select[Deleter]
                  .pipe(_(Seq(hash)))
              )
          )
      )
    }
  }

  it should "create a new item" in {
    forAll { (item: Item) =>
      Runtime.default.unsafeRun(
        createRepository(Nil, Nil)
          .pipe(repository =>
            (for {
              newItemHashes <- repository.select[Creator].pipe(_(Seq(item)))
              possibleItems <- repository.select[Reader].pipe(_(Seq(item.pipe(hashFromItem))))
              expectedItems <- possibleItems.select[Seq[Item]].pipe(IO.fromOption(_))
            } yield expectedItems shouldBe Seq(item)).map(_ =>
              repository
                .select[Deleter]
                .pipe(_(Seq(item.pipe(hashFromItem))))
            )
          )
      )
    }
  }
}