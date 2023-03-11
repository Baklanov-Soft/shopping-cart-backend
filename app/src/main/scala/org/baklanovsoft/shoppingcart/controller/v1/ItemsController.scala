package org.baklanovsoft.shoppingcart.controller.v1

import cats.MonadThrow
import cats.implicits._
import org.baklanovsoft.shoppingcart.catalog.ItemsService
import org.baklanovsoft.shoppingcart.catalog.model.{CreateItem, Item, ItemId, UpdateItem}
import org.baklanovsoft.shoppingcart.controller.v1.ControllerDomain._
import org.baklanovsoft.shoppingcart.user.model.Role
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import org.typelevel.log4cats.{Logger, LoggerFactory}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import ErrorHandler._

final case class ItemsController[F[_]: MonadThrow: Logger] private (
    auth: Auth[F],
    itemsService: ItemsService[F]
) extends Controller[F] {

  private val get =
    ItemsController.get
      .serverLogicSuccess { maybeBrandName =>
        maybeBrandName.fold(
          itemsService.findAll
        )(b => itemsService.findBy(b.toDomain))
      }

  private val getById =
    ItemsController.getById
      .serverLogic { itemId =>
        itemsService
          .findById(itemId)
          .map(_.fold((StatusCode.NotFound, "Item not found by ID").asLeft[Item])(_.asRight))
      }

  private val createItem =
    ItemsController.createItem
      .serverSecurityLogic(auth.authWithStatus(Role.Admin))
      .serverLogic { _ => item =>
        withErrorHandler(
          itemsService.create(item)
        )
      }

  private val updateItem =
    ItemsController.updateItem
      .serverSecurityLogic(auth.authWithStatus(Role.Admin))
      .serverLogic { _ => item =>
        withErrorHandler(
          itemsService.update(item)
        )
      }

  override val routes =
    List(
      get,
      getById,
      createItem,
      updateItem
    )
}

object ItemsController extends RestCodecs {

  def make[F[_]: MonadThrow: LoggerFactory](
      auth: Auth[F],
      itemsService: ItemsService[F]
  ) = {
    implicit val l = LoggerFactory.getLogger[F]
    ItemsController[F](auth, itemsService)
  }

  private val tag  = "Items"
  private val base = Routes.base / "items"

  private val get =
    endpoint.get
      .in(base)
      .in(query[Option[BrandParam]]("brandName"))
      .out(jsonBody[List[Item]])
      .tag(tag)
      .summary("Get all items by brand or all items")

  private val getById =
    endpoint.get
      .in(base)
      .in(query[ItemId]("itemId"))
      .out(jsonBody[Item])
      .errorOut(statusCode)
      .errorOut(plainBody[String])
      .tag(tag)
      .summary("Get item by id")

  private val createItem =
    Routes.secureEndpoint.post
      .in(base)
      .in(jsonBody[CreateItem])
      .out(jsonBody[ItemId])
      .errorOut(statusCode)
      .errorOut(plainBody[String])
      .tag(Routes.adminTag)
      .summary("Create item")

  private val updateItem =
    Routes.secureEndpoint.put
      .in(base)
      .in(jsonBody[UpdateItem])
      .out(jsonBody[Unit])
      .errorOut(statusCode)
      .errorOut(plainBody[String])
      .tag(Routes.adminTag)
      .summary("Update item")

}
