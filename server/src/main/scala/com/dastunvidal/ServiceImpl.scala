package com.dastunvidal.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dastunvidal.{
  ProductApi, ReadProductRequest, ReadProductResponse
}
import com.dastunvidal.domain.repositories.ProductRepository
import com.dastunvidal.domain.repositories.ProductRepository.RichResponse

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