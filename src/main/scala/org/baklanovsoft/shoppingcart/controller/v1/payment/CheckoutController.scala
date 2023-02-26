package org.baklanovsoft.shoppingcart.controller.v1.payment

import cats.effect.kernel.Sync
import cats.implicits._
import org.baklanovsoft.shoppingcart.controller.v1.{Auth, Controller, Routes}
import org.baklanovsoft.shoppingcart.model.payment.{Card, OrderId}
import org.baklanovsoft.shoppingcart.service.payment.CheckoutService
import org.baklanovsoft.shoppingcart.service.payment.CheckoutService.CheckoutError
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._

import scala.annotation.nowarn

final case class CheckoutController[F[_]: Sync](checkoutService: CheckoutService[F], auth: Auth[F])
    extends Controller[F] {

  @nowarn // if there is a non-domain error or new error - let it fail with 500
  private val mapError: Throwable => (StatusCode, String) = {
    case CheckoutError.EmptyCartError  => StatusCode.BadRequest -> "Cart is empty"
    case CheckoutError.OrderError(e)   => StatusCode.BadRequest -> e
    case CheckoutError.PaymentError(e) => StatusCode.BadRequest -> e
  }

  private val checkout =
    CheckoutController.checkout
      .serverSecurityLogic(auth.auth)
      .serverLogic { user => card =>
        checkoutService
          .process(user.id, card)
          .attempt
          .map(_.left.map(mapError))
      }

  override val routes = List(
    checkout
  )
}

object CheckoutController extends RestCodecs {
  private val tag  = "Checkout"
  private val base = Routes.base / "checkout"

  private val checkout =
    Routes.secureEndpoint.post
      .in(base)
      .in(jsonBody[Card])
      .out(jsonBody[OrderId])
      .tag(tag)
      .errorOut(statusCode)
      .errorOut(plainBody[String])
      .summary("Make a checkout for current user")

}
