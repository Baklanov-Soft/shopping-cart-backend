package org.baklanovsoft.shoppingcart

import cats.effect._
import cats.implicits._
import org.baklanovsoft.shoppingcart.model.user._
import org.baklanovsoft.shoppingcart.model.catalog._
import org.baklanovsoft.shoppingcart.model.health.{AppHealth, Status}
import org.baklanovsoft.shoppingcart.model.health.AppHealth.{PostgresStatus, RedisStatus}
import org.baklanovsoft.shoppingcart.model.user.{User, UserId, Username}
import org.baklanovsoft.shoppingcart.service.catalog.{BrandsService, ItemsService}
import org.baklanovsoft.shoppingcart.service.health.HealthService
import org.baklanovsoft.shoppingcart.service.user.AuthService
import squants.market.{Money, USD}

import java.util.UUID

object DummyServices {

  val brandsService = new BrandsService[IO] {
    private val brand = Brand(uuid = BrandId(UUID.randomUUID()), name = BrandName("test"))

    override def findAll: IO[List[Brand]]             = IO.pure(List(brand))
    override def create(name: BrandName): IO[BrandId] = IO.pure(BrandId(UUID.randomUUID()))
  }

  val itemsService = new ItemsService[IO] {

    private val item = Item(
      uuid = ItemId(UUID.randomUUID()),
      name = ItemName("test"),
      description = ItemDescription("test"),
      price = Money.apply(BigDecimal.decimal(100.5), USD),
      brand = Brand(uuid = BrandId(UUID.randomUUID()), name = BrandName("test")),
      category = Category(uuid = CategoryId(UUID.randomUUID()), name = CategoryName("test"))
    )

    override def findAll: IO[
      List[Item]
    ] = IO(List(item))

    override def findBy(brand: BrandName): IO[
      List[Item]
    ] = IO(List(item))

    override def findById(
        itemId: ItemId
    ): IO[Option[Item]] = IO(item.some)

    override def create(
        item: CreateItem
    ): IO[ItemId] = IO(ItemId(UUID.randomUUID()))

    override def update(
        item: UpdateItem
    ): IO[Unit] = IO.unit
  }

  val healthService = new HealthService[IO] {
    override def status: IO[AppHealth] = IO(
      AppHealth(RedisStatus(Status.Unreachable), PostgresStatus(Status.Unreachable))
    )
  }

  val authService = new AuthService[IO] {

    private val user = User(
      UserId(UUID.randomUUID()),
      Username("admin")
    )

    private val pwd          = Password("admin")
    private val correctToken = JwtToken("123")

    override def findUser(
        token: JwtToken
    ): IO[Option[User]] = IO.delay {
      if (token == correctToken) user.some
      else Option.empty[User]
    }

    override def newUser(
        username: Username,
        password: Password
    ): IO[JwtToken] = IO(JwtToken("234723743274"))

    override def login(
        username: Username,
        password: Password
    ): IO[JwtToken] =
      if (username == user.name && password == pwd) correctToken.pure[IO]
      else IO.raiseError[JwtToken](new Error("No user found"))

    override def logout(
        token: JwtToken,
        username: Username
    ): IO[Unit] = IO.unit

    override def check(
        username: Username
    ): IO[Boolean] =
      if (username == user.name) IO(true)
      else IO(false)
  }

}
