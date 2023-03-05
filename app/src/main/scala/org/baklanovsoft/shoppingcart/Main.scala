package org.baklanovsoft.shoppingcart

import cats.effect._
import cats.effect.std.Supervisor
import com.comcast.ip4s._
import fly4s.core.Fly4s
import fly4s.core.data.{Fly4sConfig, Location}
import org.baklanovsoft.shoppingcart.controller.v1._
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

import scala.concurrent.duration._

object Main extends IOApp {

  import DummyServices._

  private val auth = Auth[IO](authService)

  private val brands       = BrandsController[IO](brandsService)
  private val items        = ItemsController[IO](itemsService)
  private val health       = HealthController[IO](healthService)
  private val user         = UserController[IO](auth)
  private val shoppingCart = ShoppingCartController[IO](auth, shoppingCartService)
  private val orders       = OrdersController[IO](auth, ordersService)

  private val migrate =
    Fly4s
      .make[IO](
        url = "jdbc:postgresql://localhost/shopping-cart",
        user = Some("postgres"),
        password = Some("postgres".toCharArray),
        config = Fly4sConfig(
          table = "flyway",
          locations = Location.one("migrations")
        )
      )
      .use(_.migrate)

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

    checkout = CheckoutController[IO](auth, checkoutService)
    routes   = Routes[IO](health, user, brands, items, shoppingCart, orders, checkout)

    _ <- Resource.eval(migrate)

    _ <- serverR(routes)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
