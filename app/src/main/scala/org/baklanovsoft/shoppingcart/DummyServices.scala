package org.baklanovsoft.shoppingcart

import cats.effect._
import cats.effect.std.Supervisor
import org.baklanovsoft.shoppingcart.payment._
import org.baklanovsoft.shoppingcart.payment.model._
import org.baklanovsoft.shoppingcart.util.Background
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.loggerFactoryforSync

import java.util.UUID

object DummyServices {

  val paymentService = new PaymentService[IO] {
    override def process(
        payment: Payment
    ): IO[PaymentId] = IO(PaymentId(UUID.randomUUID()))
  }

  def checkoutService(shoppingCartService: ShoppingCartService[IO], ordersService: OrdersService[IO])(implicit
      s: Supervisor[IO]
  ) = {
    implicit val l = LoggerFactory.getLoggerFromName[IO]("Checkout service")
    implicit val b = Background.bgInstance[IO]

    CheckoutService[IO](paymentService, shoppingCartService, ordersService)
  }
}
