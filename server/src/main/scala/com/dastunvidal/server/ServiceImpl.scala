package com.dastunvidal.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dastunvidal.{
  ProductApi, ReadProductRequest, ReadProductResponse
}
import com.dastunvidal.domain.repositories.ProductRepository.{
  RichResponse, RichString
}
import scala.util.chaining.scalaUtilChainingOps

trait ServiceImpl extends ProductApi {
  def readProduct(request: ReadProductRequest): Source[ReadProductResponse, NotUsed] =
    request
      .identifiers
      .pipe(Source(_))
      .map(_.toDomain)
      .map(_.toContractResponse)
}

object ServiceImpl {
  def apply(): ServiceImpl = new ServiceImpl {}
}