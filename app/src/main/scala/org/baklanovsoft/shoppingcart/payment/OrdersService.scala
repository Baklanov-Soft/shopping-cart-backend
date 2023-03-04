package org.baklanovsoft.shoppingcart.payment

import cats.data.NonEmptyList
import org.baklanovsoft.shoppingcart.payment.model._
import org.baklanovsoft.shoppingcart.user.model.UserId
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
