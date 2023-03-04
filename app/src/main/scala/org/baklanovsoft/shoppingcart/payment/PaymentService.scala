package org.baklanovsoft.shoppingcart.payment
import org.baklanovsoft.shoppingcart.payment.model._

trait PaymentService[F[_]] {
  def process(payment: Payment): F[PaymentId]
}
