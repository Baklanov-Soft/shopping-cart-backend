package org.baklanovsoft.shoppingcart.payment

import cats.data.NonEmptyList
import cats.effect.Resource
import cats.effect.kernel.Concurrent
import cats.implicits._
import org.baklanovsoft.shoppingcart.error.DomainError
import org.baklanovsoft.shoppingcart.payment.model._
import org.baklanovsoft.shoppingcart.user.model.UserId
import org.baklanovsoft.shoppingcart.user.sql.UsersSQL
import org.baklanovsoft.shoppingcart.util.GenUUID
import skunk.Session
import skunk.implicits._
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

object OrdersService {

  case object UserNotFound extends DomainError {
    val code = "UserNotFound"; val status = 404; val description = None
  }

  def make[F[_]: Concurrent: GenUUID](
      sessionR: Resource[F, Session[F]]
  ): OrdersService[F] =
    new OrdersService[F] {
      import org.baklanovsoft.shoppingcart.payment.sql.OrdersSQL._

      override def get(
          userId: UserId,
          orderId: OrderId
      ): F[Option[Order]] = sessionR.use { s =>
        s.prepare(selectByUserIdAndOrderId).flatMap(_.option(userId ~ orderId))
      }

      override def findBy(
          userId: UserId
      ): F[List[Order]] = sessionR.use { s =>
        s.execute(selectByUserId, userId)
      }

      override def create(
          userId: UserId,
          pid: PaymentId,
          items: NonEmptyList[CartItem],
          total: Money
      ): F[OrderId] = sessionR.use { s =>
        for {
          maybeUser <- s.prepare(UsersSQL.selectUserById).flatMap(_.option(userId))
          _         <- Concurrent[F].fromOption(maybeUser, UserNotFound)

          orderId <- GenUUID[F].make.map(OrderId.apply)

          itMap = items.toList.map(x => x.item.uuid -> x.quantity).toMap
          order = Order(orderId, pid, itMap, total)

          _ <- s.prepare(insertOrder).flatMap(_.execute(userId ~ order))

        } yield orderId
      }
    }
}
