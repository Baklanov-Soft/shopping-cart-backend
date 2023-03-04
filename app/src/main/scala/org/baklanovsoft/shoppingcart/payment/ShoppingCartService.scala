package org.baklanovsoft.shoppingcart.payment
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.payment.model._
import org.baklanovsoft.shoppingcart.user.model.UserId

trait ShoppingCartService[F[_]] {
  def add(
      userId: UserId,
      itemId: ItemId,
      quantity: Quantity
  ): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def delete(userId: UserId): F[Unit]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: List[CartItem]): F[Unit]
}
