package org.baklanovsoft.shoppingcart

import cats.effect._
import cats.effect.std.Supervisor
import com.comcast.ip4s._
import org.baklanovsoft.shoppingcart.controller.v1.{Auth, Routes}
import org.baklanovsoft.shoppingcart.controller.v1.catalog.{BrandsController, ItemsController}
import org.baklanovsoft.shoppingcart.controller.v1.health.HealthController
import org.baklanovsoft.shoppingcart.controller.v1.payment.{
  CheckoutController,
  OrdersController,
  ShoppingCartController
}
import org.baklanovsoft.shoppingcart.controller.v1.user.UserController
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

object Main extends IOApp {

  import DummyServices._

  private val auth = Auth[IO](authService)

  private val brands       = BrandsController[IO](brandsService)
  private val items        = ItemsController[IO](itemsService)
  private val health       = HealthController[IO](healthService)
  private val user         = UserController[IO](auth)
  private val shoppingCart = ShoppingCartController[IO](auth, shoppingCartService)
  private val orders       = OrdersController[IO](auth, ordersService)

  private def serverR(routes: Routes[IO]): Resource[IO, Server] = {
    val router: HttpApp[IO] = Router.apply[IO]("/" -> routes.http4sRoutes).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(router)
      .build
  }

  private val resource = for {
    implicit0(s: Supervisor[IO]) <- Supervisor[IO]

    checkout = CheckoutController[IO](auth, checkoutService)
    routes   = Routes[IO](health, user, brands, items, shoppingCart, orders, checkout)

    _ <- serverR(routes)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
