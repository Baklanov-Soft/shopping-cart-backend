package org.baklanovsoft.shoppingcart.controller.v1

import cats.MonadThrow
import org.baklanovsoft.shoppingcart.catalog.BrandsService
import org.baklanovsoft.shoppingcart.catalog.model.{Brand, BrandId, BrandName}
import org.baklanovsoft.shoppingcart.controller.v1.ErrorHandler._
import org.baklanovsoft.shoppingcart.user.model.Role
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import org.typelevel.log4cats.{Logger, LoggerFactory}
import sttp.tapir._
import sttp.tapir.json.circe._

final case class BrandsController[F[_]: MonadThrow: Logger] private (
    auth: Auth[F],
    brandsService: BrandsService[F]
) extends Controller[F] {

  private val get =
    BrandsController.get
      .serverLogicSuccess { _ =>
        brandsService.findAll
      }

  private val post =
    BrandsController.post
      .serverSecurityLogic(auth.authWithStatus(Role.Admin))
      .serverLogic { _ => b =>
        withErrorHandler(
          brandsService.create(b)
        )
      }

  override val routes = List(
    get,
    post
  )
}

object BrandsController extends RestCodecs {

  def make[F[_]: MonadThrow: LoggerFactory](
      auth: Auth[F],
      brandsService: BrandsService[F]
  ): BrandsController[F] = {
    implicit val l = LoggerFactory.getLogger[F]
    BrandsController[F](auth, brandsService)
  }

  private val tag  = "Brands"
  private val base = Routes.base / "brands"

  private val get =
    endpoint.get
      .in(base)
      .out(jsonBody[List[Brand]])
      .tag(tag)
      .summary("Get all brands")

  private val post =
    Routes.secureEndpoint.post
      .in(base)
      .in(query[BrandName]("brandName"))
      .out(plainBody[BrandId])
      .errorOut(statusCode)
      .errorOut(plainBody[String])
      .tag(Routes.adminTag)
      .summary("Add brand")

}
