package org.baklanovsoft.shoppingcart.controller.v1

import org.baklanovsoft.shoppingcart.payment.OrdersService
import org.baklanovsoft.shoppingcart.payment.model.{Order, OrderId}
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir._
import sttp.tapir.json.circe._

final case class OrdersController[F[_]](auth: Auth[F], ordersService: OrdersService[F]) extends Controller[F] {

  private val getAll =
    OrdersController.getAll
      .serverSecurityLogic(auth.auth)
      .serverLogicSuccess { user => _ =>
        ordersService.findBy(user.userId)
      }

  private val find =
    OrdersController.find
      .serverSecurityLogic(auth.auth)
      .serverLogicSuccess { user => orderId =>
        ordersService
          .get(user.userId, orderId)

      }

  override val routes = List(
    getAll,
    find
  )
}

object OrdersController extends RestCodecs {
  private val tag  = "Orders"
  private val base = Routes.base / "orders"

  private val getAll =
    Routes.secureEndpoint.get
      .in(base)
      .out(jsonBody[List[Order]])
      .tag(tag)
      .summary("Orders list for current user")

  private val find =
    Routes.secureEndpoint.get
      .in(base)
      .in(query[OrderId]("orderId"))
      .out(jsonBody[Option[Order]])
      .tag(tag)
      .summary("Find order")

}
