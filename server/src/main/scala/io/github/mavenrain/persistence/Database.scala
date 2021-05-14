package io.github.mavenrain.persistence

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
import shapeless.{:+:, CNil}
import shapeless.syntax.inject.InjectSyntax
import zio.prelude.Newtype

object Database {
  object Error extends Newtype[Throwable]
  type Error = Error.Type
  val dataSource =
    new HikariDataSource()
      .tap(_.setMaximumPoolSize(32))
      .tap(_.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"))
      .tap(_.addDataSourceProperty("username", "oeo"))
      .tap(_.addDataSourceProperty("password", "oeo"))
      .tap(_.setAutoCommit(false))
  def connection = Try(dataSource.getConnection()).fold(
    error => Error(error).inject[Connection :+: Error :+: CNil],
    _.inject[Connection :+: Error :+: CNil]
  )
}