package org.baklanovsoft.shoppingcart.catalog
import org.baklanovsoft.shoppingcart.catalog.model._

trait BrandsService[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[BrandId]
}
