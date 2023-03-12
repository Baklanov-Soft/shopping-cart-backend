package org.baklanovsoft.shoppingcart.payment

import cats.implicits._
import cats.MonadThrow
import dev.profunktor.redis4cats.RedisCommands
import org.baklanovsoft.shoppingcart.catalog.ItemsService
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.payment.model._
import org.baklanovsoft.shoppingcart.user.model.UserId
import org.baklanovsoft.shoppingcart.util.GenUUID
import squants.market.{Money, USD}

import scala.concurrent.duration._

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

object ShoppingCartService {

  def make[F[_]: GenUUID: MonadThrow](
      itemsService: ItemsService[F],
      redis: RedisCommands[F, String, String]
  ): ShoppingCartService[F] = new ShoppingCartService[F] {

    private val CART_EXPIRATION = 3.hours

    override def add(
        userId: UserId,
        itemId: ItemId,
        quantity: Quantity
    ): F[Unit] =
      redis.hSet(userId.show, itemId.show, quantity.show) *>
        redis.expire(userId.show, CART_EXPIRATION).void // reset expiration after each cart modification

    override def get(
        userId: UserId
    ): F[CartTotal] =
      redis.hGetAll(userId.show).flatMap {
        _.toList
          .traverseFilter { case (k, v) =>
            for {
              id <- GenUUID[F].read(k).map(ItemId.apply)
              qt <- MonadThrow[F].catchNonFatal(Quantity(v.toInt))
              rs <- itemsService.findById(id).map(_.map(i => i -> qt))
            } yield rs
          }
          .map { items =>
            val cartItems = items.map { case (i, q) => CartItem(i, q) }
            val total     = cartItems.view.map(_.subTotal).fold(Money(0, USD))(_ + _)
            CartTotal(cartItems, total)
          }
      }

    override def delete(userId: UserId): F[Unit] =
      redis.del(userId.show).void

    override def removeItem(
        userId: UserId,
        itemId: ItemId
    ): F[Unit] =
      redis.hDel(userId.show, itemId.show).void

    override def update(
        userId: UserId,
        cart: Cart
    ): F[Unit] = redis.hGetAll(userId.show).flatMap {
      _.toList.traverse_ { case (k, _) =>
        GenUUID[F].read(k).map(ItemId.apply).flatMap { id =>
          cart.items.get(id).traverse_ { q =>
            redis.hSet(userId.show, k, q.show)
          }
        }
      }
    } *>
      redis.expire(userId.show, CART_EXPIRATION).void

  }
}
