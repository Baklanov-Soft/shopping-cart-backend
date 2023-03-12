package org.baklanovsoft.shoppingcart

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.std.Supervisor
import cats.implicits._
import org.baklanovsoft.shoppingcart.catalog._
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.health._
import org.baklanovsoft.shoppingcart.payment._
import org.baklanovsoft.shoppingcart.payment.model._
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.Background
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import squants.market.{Money, USD}

import java.util.UUID

object DummyServices {

  private val itemsService = new ItemsService[IO] {

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

  val shoppingCartService = new ShoppingCartService[IO] {

    private val refUnsafe = Ref.unsafe[IO, Map[UserId, Cart]](Map.empty)

    override def add(
        userId: UserId,
        itemId: ItemId,
        quantity: Quantity
    ): IO[Unit] =
      refUnsafe.update { allCarts =>
        val updatedUserCart =
          allCarts
            .get(userId)
            .map { cart =>
              Cart(
                cart.items
                  .find(_._1 == itemId)
                  // if item wasn't here
                  .fold(cart.items.updated(itemId, quantity)) { case (_, q) =>
                    // if this item already was here
                    cart.items.updated(itemId, Quantity(q.value + quantity.value))
                  }
              )

            }
            .getOrElse(Cart(Map(itemId -> quantity))) // if there were no cart before

        allCarts.updated(userId, updatedUserCart)
      }

    override def get(
        userId: UserId
    ): IO[CartTotal] =
      refUnsafe.get
        .flatMap { ref =>
          ref
            .get(userId)
            .traverse { items =>
              items.items
                .map { case (itemId, quantity) =>
                  itemsService.findById(itemId).map(_.map(i => CartItem(i, quantity)))
                }
                .toList
                .sequence
                .map(_.flatten)
                .map { items =>
                  val totalPrice = items
                    .map(i => i.item.price.amount * i.quantity.value)
                    .sum

                  CartTotal(items, Money(totalPrice, USD))
                }
            }
            .map(_.getOrElse(CartTotal(List.empty, Money(0, USD))))
        }

    override def delete(userId: UserId): IO[Unit] =
      refUnsafe.update(_.view.filterKeys(_ != userId).toMap)

    override def removeItem(
        userId: UserId,
        itemId: ItemId
    ): IO[Unit] =
      refUnsafe.update { m =>
        m
          .get(userId)
          .map(x => x.items.view.filterKeys(_ != itemId).toMap)
          .map(items => m.updated(userId, Cart(items)))
          .getOrElse(m)
      }

    override def update(
        userId: UserId,
        cart: Cart
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
          items.toList.map(c => c.item.uuid -> c.quantity).toMap,
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
