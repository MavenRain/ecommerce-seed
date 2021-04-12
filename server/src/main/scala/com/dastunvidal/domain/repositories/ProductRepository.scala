package com.dastunvidal.domain.repositories

import com.dastunvidal.{Error => ContractError, First => ContractFirst, Price => ContractPrice, Product => ContractProduct, ProductCategory, Supplier}
import com.dastunvidal.ReadProductRequest.Request
import com.dastunvidal.ReadProductRequest.Request.{Empty, Identifier}
import com.dastunvidal.ReadProductResponse
import com.dastunvidal.ReadProductResponse.Response.{Empty => EmptyResponse}
import scala.util.chaining.scalaUtilChainingOps
import scalapb.UnknownFieldSet.empty
import shapeless.{::, :+:, CNil, Coproduct, Generic, HNil, Poly1}
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
    implicit def nil = at[CNil] { _ => ProductCategory() }
  }
  private object ToContractProduct extends Poly1 {
    implicit def productIdentifier = at[ProductIdentifier] { ProductIdentifier.unwrap(_) }
    implicit def category = at[Category] { _.fold(ToContractCategory).pipe(Option(_)) }
    implicit def price = at[Price.type] { _ => Some(ContractPrice()) }
    implicit def supplierIdentifier = at[SupplierIdentifier] { id => Some(Supplier(identifier = SupplierIdentifier.unwrap(id))) }
    implicit def unit = at[Unit] { _ => empty }
    implicit def nil = at[HNil] { identity }
  }
  implicit class RichResponse(response: Response) {
    val toContractResponse = response.fold(ToContractResponse)
  }
  private def toContractProduct(product: Product): ContractProduct =
    product.map(ToContractProduct).pipe(Generic[ContractProduct].from(_))
  def apply(request: Request): Response = request match {
    case Empty => Coproduct[Response](Error)
    case Identifier(_) => Coproduct[Response](ProductIdentifier("Bob") :: Coproduct[Category](First) :: Price :: SupplierIdentifier("Burgers") :: () :: HNil)
  }
}