package org.baklanovsoft.shoppingcart.service.payment

import org.baklanovsoft.shoppingcart.model.payment.{Payment, PaymentId}

trait PaymentService[F[_]] {
  def process(payment: Payment): F[PaymentId]
}
