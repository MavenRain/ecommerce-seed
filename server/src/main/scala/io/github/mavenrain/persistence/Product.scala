package io.github.mavenrain.persistence

import org.squeryl.KeyedEntity

case class Product(
  id: String,
  content: String,
  price: BigDecimal
) extends KeyedEntity[String]