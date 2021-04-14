package com.dastunvidal.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dastunvidal.{
  ProductApi, ReadProductRequest, ReadProductResponse
}
import com.dastunvidal.ReadProductRequest.Request
import com.dastunvidal.domain.repositories.ProductRepository.Retrieve
import com.dastunvidal.domain.repositories.ProductRepository.RichResponse
import shapeless.Generic

trait ServiceImpl extends ProductApi {
  def readProduct(in: Source[ReadProductRequest, NotUsed]): Source[ReadProductResponse, NotUsed] =
    in
      .map(_.request)
      .map(Generic[Request].to(_))
      .map(_.fold(Retrieve))
      .map(_.toContractResponse)
}

object ServiceImpl {
  def apply(): ServiceImpl = new ServiceImpl {}
}