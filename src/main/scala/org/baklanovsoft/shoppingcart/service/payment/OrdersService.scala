package org.baklanovsoft.shoppingcart.service.payment

import cats.data.NonEmptyList
import org.baklanovsoft.shoppingcart.model.catalog._
import org.baklanovsoft.shoppingcart.model.payment.{Order, OrderId, PaymentId}
import org.baklanovsoft.shoppingcart.model.user.UserId
import squants.market.Money

trait OrdersService[F[_]] {
  def get(
      userId: UserId,
      orderId: OrderId
  ): F[Option[Order]]

  def findBy(userId: UserId): F[List[Order]]

  def create(
      userId: UserId,
      pid: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId]

}
