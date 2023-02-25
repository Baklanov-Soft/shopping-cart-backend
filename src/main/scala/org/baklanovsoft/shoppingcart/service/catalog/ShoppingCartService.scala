package org.baklanovsoft.shoppingcart.service.catalog

import org.baklanovsoft.shoppingcart.model.catalog._
import org.baklanovsoft.shoppingcart.model.user.UserId

trait ShoppingCartService[F[_]] {
  def add(
      userId: UserId,
      itemId: ItemId,
      quantity: Quantity
  ): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def delete(userId: UserId): F[Unit]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]
}
