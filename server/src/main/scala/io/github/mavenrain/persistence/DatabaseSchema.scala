package io.github.mavenrain.persistence

import Mode.{ProductEntityDef, TypeMode}
import org.squeryl.Schema

object DatabaseSchema extends Schema {
  val productsTable = table[Product]("products")
}