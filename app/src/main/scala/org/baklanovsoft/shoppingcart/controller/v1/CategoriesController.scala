package org.baklanovsoft.shoppingcart.controller.v1

import cats.MonadThrow
import org.baklanovsoft.shoppingcart.catalog.CategoriesService
import org.baklanovsoft.shoppingcart.catalog.model.{Category, CategoryId, CategoryName}
import org.baklanovsoft.shoppingcart.controller.v1.ErrorHandler._
import org.baklanovsoft.shoppingcart.user.model.Role
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import org.typelevel.log4cats.{Logger, LoggerFactory}
import sttp.tapir._
import sttp.tapir.json.circe._

final case class CategoriesController[F[_]: MonadThrow: Logger] private (
    auth: Auth[F],
    categoriesService: CategoriesService[F]
) extends Controller[F] {

  private val get =
    CategoriesController.get
      .serverLogicSuccess { _ =>
        categoriesService.findAll
      }

  private val post =
    CategoriesController.post
      .serverSecurityLogic(auth.authWithStatus(Role.Admin))
      .serverLogic { _ => c =>
        withErrorHandler(
          categoriesService.create(c)
        )
      }

  override val routes = List(
    get,
    post
  )
}

object CategoriesController extends RestCodecs {

  def make[F[_]: MonadThrow: LoggerFactory](
      auth: Auth[F],
      categoriesService: CategoriesService[F]
  ): CategoriesController[F] = {
    implicit val l = LoggerFactory.getLogger[F]
    CategoriesController[F](auth, categoriesService)
  }

  private val tag  = "Categories"
  private val base = Routes.base / "categories"

  private val get =
    endpoint.get
      .in(base)
      .out(jsonBody[List[Category]])
      .tag(tag)
      .summary("Get all categories")

  private val post =
    Routes.secureEndpoint.post
      .in(base)
      .in(query[CategoryName]("categoryName"))
      .out(plainBody[CategoryId])
      .errorOut(statusCode)
      .errorOut(plainBody[String])
      .tag(Routes.adminTag)
      .summary("Add category")

}
