package org.baklanovsoft.shoppingcart.health

trait HealthService[F[_]] {
  def status: F[AppHealth]
}
