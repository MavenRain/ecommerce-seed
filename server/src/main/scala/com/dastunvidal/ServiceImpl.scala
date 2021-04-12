package com.dastunvidal.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dastunvidal.{
  Error, ProductApi, ReadProductRequest, ReadProductResponse
}
import com.dastunvidal.ReadProductRequest.Request.{Empty, Identifier}
import com.dastunvidal.domain.repositories.{Error => DomainError, ProductRepository}
import com.dastunvidal.domain.repositories.ProductRepository.RichResponse
import scala.util.chaining.scalaUtilChainingOps
import shapeless.{:+:, CNil, Poly1}

trait ServiceImpl extends ProductApi {
  def readProduct(in: Source[ReadProductRequest, NotUsed]): Source[ReadProductResponse, NotUsed] =
    in
      .map(_.request)
      .map(ProductRepository(_))
      .map(_.toContractResponse)
}

object ServiceImpl {
  def apply(): ServiceImpl = new ServiceImpl {}
}