package org.baklanovsoft.shoppingcart.payment
import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.payment.model.{CartItem, PaymentId}
import org.baklanovsoft.shoppingcart.user.UsersService
import org.baklanovsoft.shoppingcart.user.model.{CreateUser, Password, Username}
import skunk.Session
import squants.market.{Money, USD}
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

import java.util.UUID

class OrdersServiceSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits {

  override type Res = (UsersService[IO], OrdersService[IO])

  override def sharedResource: Resource[IO, Res] =
    global
      .getOrFailR[Resource[IO, Session[IO]]](None)(classBasedInstance)
      .map { pool =>
        val usersService  = UsersService.make[IO](pool)
        val ordersService = OrdersService.make[IO](pool)

        usersService -> ordersService
      }

  test("Orders service works") { t =>
    val (u, o) = t

    val cart = NonEmptyList.fromListUnsafe(
      List(
        CartItem(
          Item(
            ItemId(UUID.randomUUID()),
            ItemName("item"),
            ItemDescription("test item"),
            Money(100.05, USD),
            Brand(BrandId(UUID.randomUUID()), BrandName("brand")),
            Category(CategoryId(UUID.randomUUID()), CategoryName("category"))
          ),
          Quantity(2)
        )
      )
    )

    for {
      userId  <- u.create(CreateUser(Username("OrdersServiceSpec-test1"), Password("123")))
      orderId <- o.create(
                   userId = userId,
                   pid = PaymentId(UUID.randomUUID()),
                   items = cart,
                   total = Money(200.10, USD)
                 )

      readOne <- o.get(userId, orderId)
      readAll <- o.findBy(userId)
    } yield expect(readOne.exists(_.id == orderId))
      .and(
        expect(readAll.length == 1 && readAll.exists(_.id == orderId))
      )

  }
}
