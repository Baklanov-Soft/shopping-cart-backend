package org.baklanovsoft.shoppingcart.service.payment

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.implicits._
import org.baklanovsoft.shoppingcart.model.payment.{Card, CartItem, OrderId, Payment, PaymentId}
import org.baklanovsoft.shoppingcart.model.user.UserId
import org.baklanovsoft.shoppingcart.util.{Background, Retriable, Retry}
import org.typelevel.log4cats.Logger
import retry.RetryPolicies._
import squants.market.Money

import scala.concurrent.duration._
import scala.util.control.NoStackTrace

final case class CheckoutService[F[_]: MonadThrow: Retry: Background: Logger](
    paymentService: PaymentService[F],
    shoppingCartService: ShoppingCartService[F],
    ordersService: OrdersService[F]
) {

  import org.baklanovsoft.shoppingcart.service.payment.CheckoutService._

  private val retryPolicy =
    limitRetries[F](3) |+| exponentialBackoff[F](10.milliseconds)

  private def ensureNonEmpty[A](xs: List[A]): F[NonEmptyList[A]] =
    MonadThrow[F].fromOption(
      NonEmptyList.fromList(xs),
      EmptyCartError
    )

  private def processPaymentSafe(in: Payment): F[PaymentId] =
    Retry[F]
      .retry(retryPolicy, Retriable.Payments)(paymentService.process(in))
      .adaptError { case e =>
        // option because java's Throwable.getMessage may return null and Option can process that
        PaymentError(Option(e.getMessage).getOrElse("Unknown"))
      }

  private def createOrderSafe(
      userId: UserId,
      paymentId: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId] = {
    val action =
      Retry[F]
        .retry(retryPolicy, Retriable.Orders)(
          ordersService.create(userId, paymentId, items, total)
        )
        .adaptError { case e =>
          OrderError(Option(e.getMessage).getOrElse("Unknown"))
        }

    def bgAction(fa: F[OrderId]): F[OrderId] =
      fa.onError { case _ =>
        Logger[F].warn(
          s"Failed to create order for: ${paymentId.uuid}, will schedule in background"
        ) *> // if order is not created - its ok because payment was proceeded, just plan retry in background
          Background[F].schedule(bgAction(fa), 1.hour)
      }

    bgAction(action)
  }

  def process(userId: UserId, card: Card): F[OrderId] =
    for {
      // those are simply the validation, no need for error handling
      c   <- shoppingCartService.get(userId)
      its <- ensureNonEmpty(c.items)

      // third-party HTTP API which is idempotent but could fail
      pid <- processPaymentSafe(Payment(userId, c.total, card))

      // database connection could fail
      // we also could do the order creation on the background and return to user
      // right after the payment completes
      oid <- createOrderSafe(userId, pid, its, c.total)

      // it is not crucial so we don't throw error from here and just fire and forget
      _ <- shoppingCartService.delete(userId).attempt.void
    } yield oid

}

object CheckoutService {
  case class PaymentError(error: String) extends NoStackTrace
  case class OrderError(error: String)   extends NoStackTrace

  case object EmptyCartError extends NoStackTrace
}
