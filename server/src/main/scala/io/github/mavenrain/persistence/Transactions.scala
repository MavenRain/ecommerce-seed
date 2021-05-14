package io.github.mavenrain.persistence

import java.sql.DriverManager.getConnection
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import scala.util.chaining.scalaUtilChainingOps

object Transactions {
  def initializeSession() =
    Class
      .forName("org.h2.Driver")
      .pipe(_ =>
        SessionFactory.concreteFactory = Some(() =>
          Session.create(getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "oeo", "oeo"), new H2Adapter)
        )
      )
    
}