package io.github.mavenrain.persistence

import org.squeryl.{KeyedEntityDef, PrimitiveTypeMode}

object Mode {
  implicit object TypeMode extends PrimitiveTypeMode
  implicit object ProductEntityDef extends KeyedEntityDef[Product, Int] {
    def getId(a: Product) = a.id
    def isPersisted(a: Product) = true
    def idPropertyName = "id"
  }
}