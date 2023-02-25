package org.baklanovsoft.shoppingcart.service.catalog

import org.baklanovsoft.shoppingcart.model.catalog._

trait BrandsService[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[BrandId]
}
