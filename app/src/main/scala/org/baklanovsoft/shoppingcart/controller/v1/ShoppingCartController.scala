package org.baklanovsoft.shoppingcart.controller.v1

import cats.Monad
import cats.implicits._
import org.baklanovsoft.shoppingcart.catalog.model.ItemId
import org.baklanovsoft.shoppingcart.payment.ShoppingCartService
import org.baklanovsoft.shoppingcart.payment.model.{Cart, CartTotal}
import org.baklanovsoft.shoppingcart.user.model.AuthUser
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._

final case class ShoppingCartController[F[_]: Monad](
    auth: Auth[F],
    shoppingCartService: ShoppingCartService[F]
) extends Controller[F] {

  private val get =
    ShoppingCartController.getForCurrentUser
      .serverSecurityLogic[AuthUser, F](auth.auth)
      .serverLogicSuccess { (user: AuthUser) => (_: Unit) =>
        shoppingCartService.get(user.userId)
      }

  private val addItemToCart =
    ShoppingCartController.addItemToCart
      .serverSecurityLogic[AuthUser, F](auth.auth)
      .serverLogicSuccess { (user: AuthUser) => body =>
        body.items
          .map { case (id, quantity) =>
            shoppingCartService.add(user.userId, id, quantity)
          }
          .toList
          .sequence *> ().pure[F]
      }

  private val modifyItemsInCart =
    ShoppingCartController.modifyItemsInCart
      .serverSecurityLogic[AuthUser, F](auth.auth)
      .serverLogicSuccess { (user: AuthUser) => body =>
        shoppingCartService
          .update(user.userId, body)
      }

  private val deleteFromCart =
    ShoppingCartController.deleteFromCart
      .serverSecurityLogic[AuthUser, F](auth.auth)
      .serverLogicSuccess { (user: AuthUser) => body =>
        shoppingCartService
          .removeItem(user.userId, body)
      }

  override val routes = List(
    get,
    addItemToCart,
    modifyItemsInCart,
    deleteFromCart
  )
}

object ShoppingCartController extends RestCodecs {
  private val tag  = "Cart"
  private val base = Routes.base / "cart"

  private val getForCurrentUser =
    Routes.secureEndpoint.get
      .in(base)
      .out(jsonBody[CartTotal])
      .tag(tag)
      .summary("Get shopping cart of current user")

  private val addItemToCart =
    Routes.secureEndpoint.post
      .in(base)
      .in(jsonBody[Cart])
      .out(
        statusCode(StatusCode.Created)
      )
      .tag(tag)
      .summary("Add item to current user's cart")

  private val modifyItemsInCart =
    Routes.secureEndpoint.put
      .in(base)
      .in(jsonBody[Cart])
      .tag(tag)
      .summary("Modify current user's cart")

  private val deleteFromCart =
    Routes.secureEndpoint.delete
      .in(base)
      .in(query[ItemId]("itemId"))
      .tag(tag)
      .summary("Delete item from current user's cart")

}
