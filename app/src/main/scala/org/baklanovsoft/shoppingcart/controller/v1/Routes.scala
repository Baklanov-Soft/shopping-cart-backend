package org.baklanovsoft.shoppingcart.controller.v1

import cats.effect.Async
import cats.implicits._
import org.baklanovsoft.shoppingcart.user.model.JwtToken
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.swagger.{SwaggerUI, SwaggerUIOptions}

final case class Routes[F[_]: Async](
    healthController: HealthController[F],
    userController: UserController[F],
    categoriesController: CategoriesController[F],
    brandsController: BrandsController[F],
    itemsController: ItemsController[F],
    shoppingCartController: ShoppingCartController[F],
    ordersController: OrdersController[F],
    checkoutController: CheckoutController[F]
) {

  import Routes._

  private val controllers =
    List(
      healthController,
      userController,
      categoriesController,
      brandsController,
      itemsController,
      shoppingCartController,
      ordersController,
      checkoutController
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

  def http4sRoutes(serverOptions: Http4sServerOptions[F]) =
    Http4sServerInterpreter[F](serverOptions).toRoutes(routes) <+> docsRoute
}

object Routes extends RestCodecs {
  private val api     = "api"
  private val version = "v1"
  private val title   = "Shopping Cart"
  val adminTag        = "Admin"

  private[v1] val base = api / version

  private[v1] val secureEndpoint =
    endpoint
      .securityIn(auth.bearer[JwtToken]())

}
