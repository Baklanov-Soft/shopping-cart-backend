package org.baklanovsoft.shoppingcart
import cats.effect._
import com.comcast.ip4s._
import org.baklanovsoft.shoppingcart.controller.v1.Routes
import org.baklanovsoft.shoppingcart.controller.v1.catalog.BrandsController
import org.baklanovsoft.shoppingcart.model.catalog.{Brand, BrandId, BrandName}
import org.baklanovsoft.shoppingcart.service.catalog.BrandsService
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

import java.util.UUID

object Main extends IOApp {

  private val dummyService = new BrandsService[IO] {
    override def findAll: IO[List[Brand]]             = IO.pure(List.empty[Brand])
    override def create(name: BrandName): IO[BrandId] = IO.pure(BrandId(UUID.randomUUID()))
  }

  private val brands = BrandsController[IO](dummyService)

  private val routes = Routes[IO](brands)

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
