package org.baklanovsoft.shoppingcart.controller.v1

import cats.effect.kernel.Sync
import org.baklanovsoft.shoppingcart.controller.v1.ErrorHandler._
import org.baklanovsoft.shoppingcart.payment.CheckoutService
import org.baklanovsoft.shoppingcart.payment.model.{Card, OrderId}
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import org.typelevel.log4cats.{Logger, LoggerFactory}
import sttp.tapir._
import sttp.tapir.json.circe._

final case class CheckoutController[F[_]: Sync: Logger] private (auth: Auth[F], checkoutService: CheckoutService[F])
    extends Controller[F] {

  private val checkout =
    CheckoutController.checkout
      .serverSecurityLogic(auth.authWithStatus())
      .serverLogic { user => card =>
        withErrorHandler(
          checkoutService
            .process(user.userId, card)
        )
      }

  override val routes = List(
    checkout
  )
}

object CheckoutController extends RestCodecs {

  def make[F[_]: Sync: LoggerFactory](auth: Auth[F], checkoutService: CheckoutService[F]) = {
    implicit val l = LoggerFactory.getLogger[F]
    CheckoutController(auth, checkoutService)
  }

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
