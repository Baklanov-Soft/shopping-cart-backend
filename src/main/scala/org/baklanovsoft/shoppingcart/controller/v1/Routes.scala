package org.baklanovsoft.shoppingcart.controller.v1

import cats.implicits._
import cats.effect.Async
import org.baklanovsoft.shoppingcart.controller.v1.catalog.BrandsController
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.{SwaggerUI, SwaggerUIOptions}

case class Routes[F[_]: Async](
    brandsController: BrandsController[F]
) {

  import Routes._

  private val controllers = List(
    brandsController
  )

  private val routes =
    controllers
      .flatMap(_.routes)

  private val docs =
    OpenAPIDocsInterpreter()
      .serverEndpointsToOpenAPI[F](routes, title, version)
      .toYaml

  private val docsRoute =
    Http4sServerInterpreter[F]()
      .toRoutes(SwaggerUI[F](docs, SwaggerUIOptions.default.pathPrefix(List(Routes.api, Routes.version, "docs"))))

  val http4sRoutes =
    Http4sServerInterpreter[F]().toRoutes(routes) <+> docsRoute
}

object Routes {
  private val api     = "api"
  private val version = "v1"
  private val title   = "Shopping Cart"

  private[v1] val base = api / version

}
