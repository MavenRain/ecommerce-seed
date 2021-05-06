package io.github.mavenrain.persistence

import DatabaseSchema.productsTable
import java.sql.Connection
import Mode.TypeMode._
import org.squeryl.{LazySession, Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import scala.util.chaining.scalaUtilChainingOps

object Transactions {
  type SessionFactory = Option[() => LazySession]
  val query = from(productsTable) {
    product => where(product.category gt 0) select(product) orderBy(product.category.desc)
  }
  def products = query.toSeq
  val insert: Product => Product =
    productsTable.insert(_)
  val update: Product => Unit =
    productsTable.update(_)
  def initializeSession =
    Class
        .forName("org.h2.Driver")
        .pipe(_ =>
          SessionFactory.concreteFactory =
            Database.connection.select[Connection].map(connection =>
              () => Session.create(() => connection, new H2Adapter)
            )
        )
  def sessionFactory: SessionFactory =
    Class
      .forName("org.h2.Driver")
      .pipe(_ =>
        Database.connection.select[Connection].map(connection =>
          () => Session.create(() => connection, new H2Adapter)
        )
      )
  def kickTheTires =
    transaction(
      DatabaseSchema
        .create
        .pipe(_ => insert(Product("nu943infig", 0)))
    )
    
}