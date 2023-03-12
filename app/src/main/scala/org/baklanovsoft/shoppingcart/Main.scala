package org.baklanovsoft.shoppingcart

import cats.effect._
import cats.effect.std.Supervisor
import org.baklanovsoft.shoppingcart.catalog.{BrandsService, CategoriesService, ItemsService}
import org.baklanovsoft.shoppingcart.config.ApplicationConfig
import org.baklanovsoft.shoppingcart.controller.v1._
import org.baklanovsoft.shoppingcart.health.HealthService
import org.baklanovsoft.shoppingcart.http.HttpServer
import org.baklanovsoft.shoppingcart.jdbc.Database
import org.baklanovsoft.shoppingcart.payment.OrdersService
import org.baklanovsoft.shoppingcart.redis.Redis
import org.baklanovsoft.shoppingcart.user.{AdminInitService, AuthService, UsersService}
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import pureconfig.generic.auto._
import pureconfig.module.catseffect._

object Main extends IOApp {

  private val configR: Resource[IO, ApplicationConfig] =
    Resource.eval(loadConfigF[IO, ApplicationConfig])

  private val resource = for {
    implicit0(s: Supervisor[IO]) <- Supervisor[IO]

    config <- configR

    pool  <- Database.make[IO](config.database)
    redis <- Redis.make[IO](config.redis)

    /* Services */

    usersService = UsersService.make[IO](pool)
    _           <- Resource.eval(AdminInitService.makeAdminUser[IO](config.admin, usersService))

    authService = AuthService.make[IO](usersService, redis)

    categoriesService = CategoriesService.make[IO](pool)
    brandsService     = BrandsService.make[IO](pool)
    itemsService      = ItemsService.make[IO](pool)
    ordersService     = OrdersService.make[IO](pool)

    shoppingCartService = DummyServices.shoppingCartService(itemsService)

    auth = Auth[IO](authService)

    /* Controllers */

    health = HealthController[IO](HealthService.make[IO](pool, redis))

    userController = UserController.make[IO](auth)

    shoppingCart = ShoppingCartController[IO](auth, shoppingCartService)
    orders       = OrdersController[IO](auth, ordersService)

    categories = CategoriesController.make[IO](auth, categoriesService)
    brands     = BrandsController.make[IO](auth, brandsService)
    items      = ItemsController.make[IO](auth, itemsService)

    checkout =
      CheckoutController.make[IO](auth, DummyServices.checkoutService(shoppingCartService, ordersService))

    routes = Routes[IO](health, userController, categories, brands, items, shoppingCart, orders, checkout)

    _ <- HttpServer.serverR[IO](config.http, routes)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
