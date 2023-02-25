package org.baklanovsoft.shoppingcart.service.health
import org.baklanovsoft.shoppingcart.model.health.AppHealth

trait HealthService[F[_]] {
  def status: F[AppHealth]
}
