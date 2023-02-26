package org.baklanovsoft.shoppingcart.controller.v1.catalog

import org.baklanovsoft.shoppingcart.controller.v1.ControllerDomain._
import org.baklanovsoft.shoppingcart.controller.v1.{Controller, Routes}
import org.baklanovsoft.shoppingcart.model.catalog._
import org.baklanovsoft.shoppingcart.service.catalog.ItemsService
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

final case class ItemsController[F[_]](
    itemsService: ItemsService[F]
) extends Controller[F] {

  private val get =
    ItemsController.get
      .serverLogicSuccess { maybeBrandName =>
        maybeBrandName.fold(
          itemsService.findAll
        )(b => itemsService.findBy(b.toDomain))
      }

  override val routes =
    List(
      get
    )
}

object ItemsController extends RestCodecs {
  private val tag  = "Items"
  private val base = Routes.base / "items"

  private val get =
    endpoint.get
      .in(base)
      .in(query[Option[BrandParam]]("brandName"))
      .out(jsonBody[List[Item]])
      .tag(tag)

}
