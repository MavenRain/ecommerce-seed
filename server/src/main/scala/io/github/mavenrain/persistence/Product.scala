package io.github.mavenrain.persistence

import org.squeryl.KeyedEntity

case class Product(
  id: Int,
  content: String,
  price: BigDecimal
) extends KeyedEntity[Int]