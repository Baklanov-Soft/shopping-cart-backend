package org.baklanovsoft.shoppingcart

import cats.effect._
import cats.effect.std.Supervisor
import com.comcast.ip4s._
import org.baklanovsoft.shoppingcart.catalog.{BrandsService, CategoriesService, ItemsService}
import org.baklanovsoft.shoppingcart.config.ApplicationConfig
import org.baklanovsoft.shoppingcart.controller.v1._
import org.baklanovsoft.shoppingcart.jdbc.Database
import org.baklanovsoft.shoppingcart.user.{AdminInitService, AuthService, UsersService}
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.interceptor.log.DefaultServerLog

import scala.concurrent.duration._

object Main extends IOApp {

  private val health = HealthController[IO](DummyServices.healthService)

  private val configR: Resource[IO, ApplicationConfig] =
    Resource.eval(loadConfigF[IO, ApplicationConfig])

  private def serverR(routes: Routes[IO]): Resource[IO, Server] = {
    val logger = LoggerFactory.getLoggerFromName[IO]("EmberServer")

    val logSettings =
      DefaultServerLog(
        doLogWhenReceived = s => logger.debug(s),
        doLogWhenHandled = (s, t) => t.fold(logger.debug(s))(logger.error(_)(s)),
        doLogAllDecodeFailures = (s, t) => t.fold(logger.debug(s))(logger.error(_)(s)),
        doLogExceptions = (s, t) => logger.error(t)(s),
        noLog = IO.unit
      )

    val corsSettings =
      CORSInterceptor
        .customOrThrow[IO](
          CORSConfig.default.allowAllOrigins.allowAllHeaders.allowAllMethods
        )

    val options =
      Http4sServerOptions
        .customiseInterceptors[IO]
        .corsInterceptor(corsSettings)
        .serverLog(logSettings)
        .options

    val router: HttpApp[IO] = Router.apply[IO]("/" -> routes.http4sRoutes(options)).orNotFound

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

    config <- configR

    pool <- Database.make[IO](config.database)

    usersService = UsersService.make[IO](pool)
    _           <- Resource.eval(AdminInitService.makeAdminUser[IO](config.admin, usersService))

    authService <- Resource.eval(AuthService.make[IO](usersService))

    categoriesService   = CategoriesService.make[IO](pool)
    brandsService       = BrandsService.make[IO](pool)
    itemsService        = ItemsService.make[IO](pool)
    shoppingCartService = DummyServices.shoppingCartService(itemsService)

    auth = Auth[IO](authService)

    userController = UserController.make[IO](auth)

    shoppingCart = ShoppingCartController[IO](auth, shoppingCartService)
    orders       = OrdersController[IO](auth, DummyServices.ordersService)

    categories = CategoriesController.make[IO](auth, categoriesService)
    brands     = BrandsController.make[IO](auth, brandsService)
    items      = ItemsController.make[IO](auth, itemsService)

    checkout =
      CheckoutController.make[IO](auth, DummyServices.checkoutService(shoppingCartService))
    routes   = Routes[IO](health, userController, categories, brands, items, shoppingCart, orders, checkout)

    _ <- serverR(routes)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
