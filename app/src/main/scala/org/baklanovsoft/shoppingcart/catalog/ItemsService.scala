package org.baklanovsoft.shoppingcart.catalog

import org.baklanovsoft.shoppingcart.catalog.model._

trait ItemsService[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[ItemId]
  def update(item: UpdateItem): F[Unit]
}