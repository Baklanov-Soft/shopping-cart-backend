package org.baklanovsoft.shoppingcart.service.catalog
import org.baklanovsoft.shoppingcart.model.catalog._

trait ItemsService[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[Option[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[ItemId]
  def update(item: UpdateItem): F[Unit]
}
