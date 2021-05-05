package io.github.mavenrain.domain.repositories

import io.github.mavenrain.{
  Error => ContractError, First => ContractFirst, Price => ContractPrice,
  Product => ContractProduct, ProductCategory, Supplier
}
import io.github.mavenrain.ReadProductResponse
import io.github.mavenrain.ReadProductResponse.Response.{Empty => EmptyResponse}
import scala.util.chaining.scalaUtilChainingOps
import scalapb.UnknownFieldSet.empty
import shapeless.{::, :+:, CNil, Coproduct, Generic, HNil, Poly1}
import shapeless.syntax.inject.InjectSyntax
import zio.prelude.Newtype

object Error
object ProductIdentifier extends Newtype[String]
object SupplierIdentifier extends Newtype[String]
object EmptyCategory
object First
object Price

object ProductRepository {
  type Error = Error.type
  type ProductIdentifier = ProductIdentifier.Type
  type SupplierIdentifier = SupplierIdentifier.Type
  type Category = EmptyCategory.type :+: First.type :+: CNil
  type Product = ProductIdentifier :: Category :: Price.type :: SupplierIdentifier :: Unit :: HNil
  type Response = Error :+: Product :+: CNil
  private object ToContractResponse extends Poly1 {
    private val emptyResponse = ReadProductResponse(EmptyResponse) 
    implicit def error = at[Error] { _ => emptyResponse.withError(ContractError()) }
    implicit def product = at[Product] { toContractProduct(_).pipe(emptyResponse.withProduct(_)) }
  }
  private object ToContractCategory extends Poly1 {
    implicit def empty = at[EmptyCategory.type] { _ => ProductCategory() }
    implicit def first = at[First.type] { _ => ProductCategory().withFirst(ContractFirst()) }
  }
  private object ToContractProduct extends Poly1 {
    implicit def productIdentifier = at[ProductIdentifier] { ProductIdentifier.unwrap(_) }
    implicit def category = at[Category] { _.fold(ToContractCategory).pipe(Option(_)) }
    implicit def price = at[Price.type] { _ => Some(ContractPrice()) }
    implicit def supplierIdentifier = at[SupplierIdentifier] { id => Some(Supplier(identifier = SupplierIdentifier.unwrap(id))) }
    implicit def unit = at[Unit] { _ => empty }
  }
  implicit class RichResponse(response: Response) {
    val toContractResponse = response.fold(ToContractResponse)
  }
  private def toContractProduct(product: Product): ContractProduct =
    product.map(ToContractProduct).pipe(Generic[ContractProduct].from(_))
  implicit class RichString(identifier: String) {
    val toDomain = (ProductIdentifier("Bob") :: Coproduct[Category](First) :: Price :: SupplierIdentifier("Burgers") :: () :: HNil).inject[Response]
  }
}