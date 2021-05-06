package io.github.mavenrain.persistence

import shapeless.{::, :+:, CNil, HNil}
import zio.UIO

trait RepositoryProvider {
  type CreatorError
  type ReaderError
  type UpdaterError
  type DeleterError
  type Item
  type Hash
  type Creator = Item => UIO[Hash :+: CreatorError :+: CNil]
  type Reader = Hash => UIO[Item :+: ReaderError :+: CNil]
  type Updater = (Hash :: Item :: HNil) => UIO[Hash :+: UpdaterError :+: CNil]
  type Deleter = Hash => UIO[Hash :+: DeleterError :+: CNil]
  type Repository =
    Creator :: Reader :: Updater :: Deleter :: HNil 
}