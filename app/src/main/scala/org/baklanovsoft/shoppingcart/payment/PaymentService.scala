package org.baklanovsoft.shoppingcart.payment

import cats.implicits._
import cats.Applicative
import org.baklanovsoft.shoppingcart.payment.model._
import org.baklanovsoft.shoppingcart.util.GenUUID

trait PaymentService[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

object PaymentService {
  def make[F[_]: GenUUID: Applicative]: PaymentService[F] = new PaymentService[F] {
    override def process(
        payment: Payment
    ): F[PaymentId] =
      GenUUID[F].make.map(PaymentId.apply)
  }
}
