package org.baklanovsoft.shoppingcart.controller.v1

import org.baklanovsoft.shoppingcart.catalog.BrandsService
import org.baklanovsoft.shoppingcart.catalog.model.Brand
import sttp.tapir._
import sttp.tapir.json.circe._

final case class BrandsController[F[_]](
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
    endpoint.get
      .in(base)
      .out(jsonBody[List[Brand]])
      .tag(tag)
      .summary("Get all brands")

}
