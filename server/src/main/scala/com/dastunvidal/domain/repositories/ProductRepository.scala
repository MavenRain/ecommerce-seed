package com.dastunvidal.domain.repositories

import com.dastunvidal.{Error => ContractError, First => ContractFirst, Price => ContractPrice, Product => ContractProduct, ProductCategory, Supplier}
import com.dastunvidal.ReadProductRequest.Request
import com.dastunvidal.ReadProductRequest.Request.{Empty, Identifier}
import com.dastunvidal.ReadProductResponse
import com.dastunvidal.ReadProductResponse.Response.{Empty => EmptyResponse}
import scala.util.chaining.scalaUtilChainingOps
import shapeless.{::, :+:, CNil, Coproduct, HNil, Poly1}
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
  type Product = ProductIdentifier :: Category :: Price.type :: SupplierIdentifier :: HNil
  private val aProduct =
    ContractProduct(identifier = "bar")
      .withCategory(ProductCategory().withFirst(ContractFirst()))
      .withPrice(ContractPrice())
      .withSupplier(Supplier(identifier = "acme"))
  type Response = Error :+: Product :+: CNil
  object ToContractResponse extends Poly1 {
    private val emptyResponse = ReadProductResponse(EmptyResponse) 
    implicit def error = at[Error] { _ => emptyResponse.withError(ContractError()) }
    implicit def product = at[Product] { toContractProduct(_).pipe(contractProduct => emptyResponse.withProduct(contractProduct)) }
  }
  implicit class RichResponse(response: Response) {
    val toContractResponse = response.fold(ToContractResponse)
  }
  private def toContractProduct(product: Product): ContractProduct =
    ContractProduct(identifier = ProductIdentifier.unwrap(product.select[ProductIdentifier]))
      .withCategory(ProductCategory().withFirst(ContractFirst()))
      .withPrice(ContractPrice())
      .withSupplier(Supplier(identifier = SupplierIdentifier.unwrap(product.select[SupplierIdentifier])))
  def apply(request: Request): Response = request match {
    case Empty => Coproduct[Response](Error)
    case Identifier(_) => Coproduct[Response](ProductIdentifier("Bob") :: Coproduct[Category](First) :: Price :: SupplierIdentifier("Burgers") :: HNil)
  }
}