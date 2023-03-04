package org.baklanovsoft.shoppingcart.catalog
import org.baklanovsoft.shoppingcart.catalog.model._

trait CategoriesService[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[CategoryId]
}
