package io.github.mavenrain.persistence

import DatabaseSchema.productsTable
import Mode.TypeMode._
import org.junit.runner.RunWith
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaNumStr
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
import shapeless.{:+:, CNil, HNil}
import shapeless.syntax.inject.InjectSyntax
import zio.{IO, UIO}
import zio.prelude.Newtype

@RunWith(classOf[JUnitRunner])
class ProductRepositoryLaws
  extends AnyFlatSpec
  with Matchers
  with RepositoryLaws
  with RepositoryProvider
  with ScalaCheckDrivenPropertyChecks {
  object CreatorErrorType extends Newtype[Throwable]
  type CreatorError = CreatorErrorType.Type
  object ReaderErrorType extends Newtype[Throwable]
  type ReaderError = ReaderErrorType.Type
  object UpdateErrorType extends Newtype[Throwable]
  type UpdaterError = UpdateErrorType.Type
  object DeleterErrorType extends Newtype[Throwable]
  type DeleterError = DeleterErrorType.Type
  type Item = Product
  type Hash = String
  private val creator: Creator =
    creatorItems =>
      Try(transaction(creatorItems.map(productsTable.insert(_))))
        .pipe(IO.fromTry(_))
        .either
        .map(_.fold(
          CreatorErrorType(_).inject[Seq[Hash] :+: CreatorError :+: CNil],
          _.map(_.id).inject[Seq[Hash] :+: CreatorError :+: CNil]
        ))
  private val reader: Reader =
    readerItems =>
      Try(transaction(from(productsTable) { product =>
        where(product.id in readerItems) select(product)
      }.toSeq.distinct))
      .pipe(IO.fromTry(_))
      .either
      .map(_.fold(
        ReaderErrorType(_).inject[Seq[Item] :+: ReaderError :+: CNil],
        _.inject[Seq[Item] :+: ReaderError :+: CNil]
      ))
  private val updater: Updater =
    _ => UIO.succeed(Seq[Hash]().inject[Seq[Hash] :+: UpdaterError :+: CNil])
  private val deleter: Deleter =
    deleterItems =>
      Try(transaction(
        deleterItems.map(item => productsTable.deleteWhere(_.id === item)).sum
      ))
      .pipe(IO.fromTry(_))
      .either
      .map(_.fold(
        DeleterErrorType(_).inject[RowsDeleted :+: DeleterError :+: CNil],
        RowsDeleted(_).inject[RowsDeleted :+: DeleterError :+: CNil]
      ))
  protected val createRepository: RepositoryCreator =
    (items, hashes) => (
        transaction(
          items
            .zip(hashes)
            .collect { case (item, hash) =>
              productsTable.insert(item.copy(id = hash))
            }
            .distinct
        )
        .pipe(_ => creator :: reader :: updater :: deleter :: HNil)
    )
  override implicit protected val arbitraryId: Arbitrary[Hash] = Arbitrary(alphaNumStr)
  override implicit protected val arbitraryItem: Arbitrary[Item] = Arbitrary(for {
    identifier <- arbitrary[String]
    text <- arbitrary[String]
    monetaryValue <- arbitrary[BigDecimal]
  } yield Product(id = identifier, content = text, price = monetaryValue))
  protected def expectedItems(items: Seq[Item], hashes: Seq[Hash]) =
    items.zip(hashes).collect { case (item, hash) => item.copy(id = hash) }
  protected def createSchema = inTransaction(DatabaseSchema.create)
  protected def hashFromItem(item: Item) = item.id
}