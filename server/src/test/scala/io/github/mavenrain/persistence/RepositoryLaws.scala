package io.github.mavenrain.persistence

import java.sql.DriverManager.getConnection
import org.scalacheck.Arbitrary
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.squeryl.Session.create
import org.squeryl.SessionFactory.concreteFactory
import org.squeryl.adapters.H2Adapter
import scala.util.chaining.scalaUtilChainingOps
import zio.{IO, Runtime}

trait RepositoryLaws  {
  this: AnyFlatSpec with Matchers with RepositoryProvider with ScalaCheckDrivenPropertyChecks =>
  protected type RepositoryCreator = Seq[Item] => Repository
  protected val createRepository: RepositoryCreator
  protected implicit val arbitraryItem: Arbitrary[Item]
  protected def createSchema: Unit
  protected def hashFromItem(item: Item): Hash

  // Session initialization
  Class.forName("org.h2.Driver")
  concreteFactory = Some(() => create(getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "oeo", "oeo"), new H2Adapter))
  createSchema

  it should "read an existing item" in {
    forAll { (item: Item) =>
      Runtime.default.unsafeRun(
        Seq(item)
          .pipe(createRepository)
          .pipe(repository =>
            repository
              .select[Reader]
              .pipe(_(Seq(item).map(hashFromItem)))
              .flatMap(_.select[Seq[Item]].pipe(IO.fromOption(_)))
              .map(_ shouldBe Seq(item))
              .map(_ =>
                repository
                  .select[Deleter]
                  .pipe(_(Seq(item).map(hashFromItem)))
              )
          )
      )
    }
  }

  it should "create a new item" in {
    forAll { (item: Item) =>
      Runtime.default.unsafeRun(
        Nil
          .pipe(createRepository)
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

  it should "delete an existing item" in {
    forAll { (item: Item) =>
      Runtime.default.unsafeRun(
        Seq(item)
          .pipe(createRepository)
          .pipe(repository => for {
            possibleRowCount <- repository.select[Deleter].pipe(_(Seq(item).map(hashFromItem)))
            rowCount <- possibleRowCount.select[RowsDeleted].map(RowsDeleted.unwrap(_)).pipe(IO.fromOption(_))
          } yield rowCount shouldBe 1)
      )
    }
  }
}