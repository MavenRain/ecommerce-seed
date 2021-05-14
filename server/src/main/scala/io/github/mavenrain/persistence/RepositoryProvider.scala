package io.github.mavenrain.persistence

import shapeless.{::, :+:, CNil, HNil}
import zio.UIO
import zio.prelude.Newtype

trait RepositoryProvider {
  type CreatorError
  type ReaderError
  type UpdaterError
  type DeleterError
  type Item
  type Hash
  object RowsDeleted extends Newtype[Int]
  type RowsDeleted = RowsDeleted.Type
  type Creator = Seq[Item] => UIO[Seq[Hash] :+: CreatorError :+: CNil]
  type Reader = Seq[Hash] => UIO[Seq[Item] :+: ReaderError :+: CNil]
  type Updater = Seq[Hash :: Item :: HNil] => UIO[Seq[Hash] :+: UpdaterError :+: CNil]
  type Deleter = Seq[Hash] => UIO[RowsDeleted :+: DeleterError :+: CNil]
  type Repository =
    Creator :: Reader :: Updater :: Deleter :: HNil 
}