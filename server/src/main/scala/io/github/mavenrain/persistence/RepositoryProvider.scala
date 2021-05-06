package io.github.mavenrain.persistence

import shapeless.{::, :+:, CNil, HNil}
import zio.UIO

trait RepositoryProvider {
  type Error
  type Creator[Item] = Item => UIO[Unit :+: Error :+: CNil]
  type Reader[Id, Item] = Id => UIO[Item :+: Error :+: CNil]
  type Updater[Id, Item] = (Id :: Item :: HNil) => UIO[Unit :+: Error :+: CNil]
  type Deleter[Id] = Id => UIO[Unit :+: Error :+: CNil]
  type Repository[Id, Item] =
    Creator[Item] :: Reader[Id, Item] :: Updater[Id, Item] :: Deleter[Id] :: HNil 
}