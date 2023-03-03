package org.baklanovsoft.shoppingcart
import cats.effect._
import com.comcast.ip4s._
import org.baklanovsoft.shoppingcart.controller.v1.{Auth, Routes}
import org.baklanovsoft.shoppingcart.controller.v1.catalog.{BrandsController, ItemsController}
import org.baklanovsoft.shoppingcart.controller.v1.health.HealthController
import org.baklanovsoft.shoppingcart.controller.v1.user.UserController
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

object Main extends IOApp {

  import DummyServices._

  private val brands = BrandsController[IO](brandsService)
  private val items  = ItemsController[IO](itemsService)
  private val health = HealthController[IO](healthService)
  private val user   = UserController[IO](Auth[IO](authService))

  private val routes = Routes[IO](health, user, brands, items)

  private val router = Router("/" -> routes.http4sRoutes).orNotFound

  private val server: Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(router)
      .build

  override def run(args: List[String]): IO[ExitCode] =
    server
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
