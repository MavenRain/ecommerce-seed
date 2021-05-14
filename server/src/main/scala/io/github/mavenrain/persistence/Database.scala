package io.github.mavenrain.persistence

import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Connection
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
import shapeless.{:+:, CNil}
import shapeless.syntax.inject.InjectSyntax
import zio.prelude.Newtype

object Database {
  object Error extends Newtype[Throwable]
  type Error = Error.Type
  private val dataSource =
    new ComboPooledDataSource()
      .tap(_.setDriverClass("org.h2.Driver"))
      .tap(_.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"))
      .tap(_.setUser("oeo"))
      .tap(_.setPassword("oeo"))
      .tap(_.setMinPoolSize(1))
      .tap(_.setMaxPoolSize(32))
      .tap(_.setAcquireIncrement(1))

  def connection = Try(dataSource.getConnection()).fold(
    error => Error(error).inject[Connection :+: Error :+: CNil],
    _.inject[Connection :+: Error :+: CNil]
  )
}