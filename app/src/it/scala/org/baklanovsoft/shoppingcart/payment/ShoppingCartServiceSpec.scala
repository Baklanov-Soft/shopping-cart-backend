package org.baklanovsoft.shoppingcart.payment

import cats.effect.IO
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.ResourcesRegistry.Redis
import org.baklanovsoft.shoppingcart.catalog.ItemsService
import org.baklanovsoft.shoppingcart.catalog.model.{ItemId, Quantity}
import org.baklanovsoft.shoppingcart.user.model.UserId
import org.baklanovsoft.shoppingcart.util.GenUUID
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}
import org.scalamock.scalatest.MockFactory

class ShoppingCartServiceSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits with MockFactory {

  override type Res = ShoppingCartService[IO]

  private val itemsMock = mock[ItemsService[IO]]

  override def sharedResource: Resource[IO, Res] =
    global
      .getOrFailR[Redis](None)(classBasedInstance)
      .map { redis =>
        ShoppingCartService.make[IO](itemsMock, redis)
      }

  test("Shopping cart service works") { shoppingCartService =>
    for {
      itemId <- GenUUID[IO].make.map(ItemId.apply)
      userId <- GenUUID[IO].make.map(UserId.apply)

      _ <- shoppingCartService.add(userId, itemId, Quantity(1))

    } yield expect(true)
  }

}
