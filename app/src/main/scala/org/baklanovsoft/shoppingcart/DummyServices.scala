package org.baklanovsoft.shoppingcart

import cats.effect._
import org.baklanovsoft.shoppingcart.payment._
import org.baklanovsoft.shoppingcart.payment.model._

import java.util.UUID

object DummyServices {

  val paymentService = new PaymentService[IO] {
    override def process(
        payment: Payment
    ): IO[PaymentId] = IO(PaymentId(UUID.randomUUID()))
  }
}
