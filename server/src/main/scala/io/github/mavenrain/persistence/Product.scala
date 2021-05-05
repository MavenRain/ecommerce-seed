package io.github.mavenrain.persistence

import org.squeryl.KeyedEntity

case class Product(id: String, category: Int) extends KeyedEntity[String]