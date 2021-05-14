package io.github.mavenrain.persistence

import Mode.{ProductEntityDef, TypeMode}
import org.squeryl.Schema

object DatabaseSchema extends Schema {
  override val defaultSizeOfBigDecimal = (400, 16)
  val productsTable = table[Product]("products")
}