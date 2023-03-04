package org.baklanovsoft.shoppingcart.controller.v1

import org.baklanovsoft.shoppingcart.health.{AppHealth, HealthService}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

final case class HealthController[F[_]](healthService: HealthService[F]) extends Controller[F] {

  private val get =
    HealthController.get.serverLogicSuccess { _ =>
      healthService.status
    }

  override val routes = List(
    get
  )
}

object HealthController {
  private val tag  = "Health"
  private val base = Routes.base / "health"

  private val get =
    endpoint.get
      .in(base)
      .out(jsonBody[AppHealth])
      .tag(tag)

}
