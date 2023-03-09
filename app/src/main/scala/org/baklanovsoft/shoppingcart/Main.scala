package org.baklanovsoft.shoppingcart

import cats.effect._
import cats.effect.std.Supervisor
import com.comcast.ip4s._
import org.baklanovsoft.shoppingcart.config.DatabaseConfig
import org.baklanovsoft.shoppingcart.controller.v1._
import org.baklanovsoft.shoppingcart.jdbc.Database
import org.baklanovsoft.shoppingcart.user.{AuthService, UsersService}
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.slf4j.loggerFactoryforSync

import scala.concurrent.duration._

object Main extends IOApp {

  private val brands = BrandsController[IO](DummyServices.brandsService)
  private val items  = ItemsController[IO](DummyServices.itemsService)
  private val health = HealthController[IO](DummyServices.healthService)

  private val dbConfig = DatabaseConfig(
    database = "shopping-cart"
  )

  private def serverR(routes: Routes[IO]): Resource[IO, Server] = {
    val router: HttpApp[IO] = Router.apply[IO]("/" -> routes.http4sRoutes).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(router)
      .withShutdownTimeout(5.seconds)
      .build
  }

  private val resource = for {
    implicit0(s: Supervisor[IO]) <- Supervisor[IO]

    pool <- Database.make[IO](dbConfig)

    usersService = UsersService.make[IO](pool)
    authService <- Resource.eval(AuthService.make[IO](usersService))

    auth           = Auth[IO](authService)
    userController = UserController.make[IO](auth)
    shoppingCart   = ShoppingCartController[IO](auth, DummyServices.shoppingCartService)
    orders         = OrdersController[IO](auth, DummyServices.ordersService)

    checkout = CheckoutController.make[IO](auth, DummyServices.checkoutService)
    routes   = Routes[IO](health, userController, brands, items, shoppingCart, orders, checkout)

    _ <- serverR(routes)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
