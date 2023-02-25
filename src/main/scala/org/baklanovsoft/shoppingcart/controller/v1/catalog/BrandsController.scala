package org.baklanovsoft.shoppingcart.controller.v1.catalog

import org.baklanovsoft.shoppingcart.controller.v1.{Controller, Routes}
import org.baklanovsoft.shoppingcart.model.catalog.Brand
import org.baklanovsoft.shoppingcart.service.catalog.BrandsService
import sttp.tapir._
import sttp.tapir.json.circe._

case class BrandsController[F[_]](
    brandsService: BrandsService[F]
) extends Controller[F] {

  private val get =
    BrandsController.get
      .serverLogicSuccess { _ =>
        brandsService.findAll
      }

  override val routes = List(
    get
  )
}

object BrandsController {
  private val tag  = "Brands"
  private val base = Routes.base / "brands"

  private val get =
    endpoint
      .in(base)
      .get
      .out(jsonBody[List[Brand]])
      .tag(tag)

}
