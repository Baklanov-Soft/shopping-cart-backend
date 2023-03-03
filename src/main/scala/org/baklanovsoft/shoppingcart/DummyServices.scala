package org.baklanovsoft.shoppingcart

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.std.Supervisor
import cats.implicits._
import org.baklanovsoft.shoppingcart.model.catalog._
import org.baklanovsoft.shoppingcart.model.health.AppHealth.{PostgresStatus, RedisStatus}
import org.baklanovsoft.shoppingcart.model.health.{AppHealth, Status}
import org.baklanovsoft.shoppingcart.model.payment._
import org.baklanovsoft.shoppingcart.model.user._
import org.baklanovsoft.shoppingcart.service.catalog.{BrandsService, ItemsService}
import org.baklanovsoft.shoppingcart.service.health.HealthService
import org.baklanovsoft.shoppingcart.service.payment.{
  CheckoutService,
  OrdersService,
  PaymentService,
  ShoppingCartService
}
import org.baklanovsoft.shoppingcart.service.user.AuthService
import org.baklanovsoft.shoppingcart.util.Background
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
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

  val shoppingCartService = new ShoppingCartService[IO] {

    private val refUnsafe = Ref.unsafe[IO, Map[UserId, List[CartItem]]](Map.empty)

    private def defaultItem(itemId: ItemId) = Item(
      itemId,
      ItemName("test"),
      ItemDescription("test"),
      Money(BigDecimal.decimal(100.5), USD),
      Brand(BrandId(UUID.randomUUID()), BrandName("test brand")),
      Category(CategoryId(UUID.randomUUID()), CategoryName("test category"))
    )

    override def add(
        userId: UserId,
        itemId: ItemId,
        quantity: Quantity
    ): IO[Unit] =
      refUnsafe.update { allCarts =>
        val item = CartItem(
          defaultItem(itemId),
          quantity
        )

        val newCart =
          allCarts
            .get(userId)
            .map(cart => cart :+ item)
            .getOrElse(item :: Nil)

        allCarts.updated(userId, newCart)
      }

    override def get(
        userId: UserId
    ): IO[CartTotal] =
      refUnsafe.get
        .map(
          _.get(userId)
            .map { items =>
              val total = items.view.map(i => i.quantity.value * i.item.price.value).sum
              CartTotal(items, Money(total, USD))
            }
            .getOrElse(CartTotal(List.empty, Money(0, USD)))
        )

    override def delete(userId: UserId): IO[Unit] =
      refUnsafe.update(_.view.filterKeys(_ != userId).toMap)

    override def removeItem(
        userId: UserId,
        itemId: ItemId
    ): IO[Unit] =
      refUnsafe.update { m =>
        m
          .get(userId)
          .map(_.filterNot(_.item.uuid == itemId))
          .map(items => m.updated(userId, items))
          .getOrElse(m)
      }

    override def update(
        userId: UserId,
        cart: List[CartItem]
    ): IO[Unit] = refUnsafe.update(_.updated(userId, cart))
  }

  val paymentService = new PaymentService[IO] {
    override def process(
        payment: Payment
    ): IO[PaymentId] = IO(PaymentId(UUID.randomUUID()))
  }

  val ordersService = new OrdersService[IO] {

    private val unsafeRef = Ref.unsafe[IO, Map[UserId, List[Order]]](Map.empty)

    override def get(
        userId: UserId,
        orderId: OrderId
    ): IO[Option[Order]] =
      unsafeRef.get.map(
        _.get(userId).flatMap(_.find(_.id == orderId))
      )

    override def findBy(userId: UserId): IO[List[Order]] =
      unsafeRef.get.map(
        _.getOrElse(userId, List.empty)
      )

    override def create(
        userId: UserId,
        pid: PaymentId,
        items: NonEmptyList[CartItem],
        total: Money
    ): IO[OrderId] = {
      val id = OrderId(UUID.randomUUID())

      unsafeRef.update { all =>
        val o = Order(
          id,
          PaymentId(UUID.randomUUID()),
          items.toList,
          total
        )

        all.get(userId).map(_ :+ o).fold(all)(o => all.updated(userId, o))

      } >> id.pure[IO]
    }
  }

  def checkoutService(implicit s: Supervisor[IO]) = {
    implicit val l = LoggerFactory.getLoggerFromName[IO]("Checkout service")
    implicit val b = Background.bgInstance[IO]

    CheckoutService[IO](paymentService, shoppingCartService, ordersService)
  }
}
