package org.baklanovsoft.shoppingcart.service.catalog

import org.baklanovsoft.shoppingcart.model.catalog._

trait CategoriesService[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[CategoryId]
}
