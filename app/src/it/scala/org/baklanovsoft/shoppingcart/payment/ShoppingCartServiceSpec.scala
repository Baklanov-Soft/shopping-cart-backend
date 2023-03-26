package org.baklanovsoft.shoppingcart.payment

import cats.effect.IO
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.ResourcesRegistry.Redis
import org.baklanovsoft.shoppingcart.catalog.ItemsService
import org.baklanovsoft.shoppingcart.catalog.model.{
  Brand,
  BrandId,
  BrandName,
  Category,
  CategoryId,
  CategoryName,
  Item,
  ItemDescription,
  ItemId,
  ItemName,
  Quantity
}
import org.baklanovsoft.shoppingcart.payment.model.Cart
import org.baklanovsoft.shoppingcart.user.model.UserId
import org.baklanovsoft.shoppingcart.util.GenUUID
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.mockito.cats.MockitoCats
import squants.market.{Money, USD}
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

class ShoppingCartServiceSpec(global: GlobalRead)
    extends IOSuite
    with LowPriorityImplicits
    with MockitoSugar
    with ArgumentMatchersSugar
    with MockitoCats {

  override type Res = ShoppingCartService[IO]

  private val itemsMock = mock[ItemsService[IO]]

  override def sharedResource: Resource[IO, Res] =
    global
      .getOrFailR[Redis](None)(classBasedInstance)
      .map { redis =>
        ShoppingCartService.make[IO](itemsMock, redis)
      }

  test("CRUD shopping cart work") { shoppingCartService =>
    for {
      itemId1 <- GenUUID[IO].make.map(ItemId.apply)
      itemId2 <- GenUUID[IO].make.map(ItemId.apply)

      userId <- GenUUID[IO].make.map(UserId.apply)

      brandId    <- GenUUID[IO].make.map(BrandId.apply)
      categoryId <- GenUUID[IO].make.map(CategoryId.apply)

      item1 = Item(
                itemId1,
                ItemName("ShoppingCartServiceSpec-test-item-1"),
                ItemDescription("Test description"),
                Money(100.5, USD),
                Brand(
                  brandId,
                  BrandName("ShoppingCartServiceSpec-test-brand-1")
                ),
                Category(
                  categoryId,
                  CategoryName("ShoppingCartServiceSpec-test-cat-1")
                )
              )

      item2 = item1.copy(uuid = itemId2, price = Money(300.1, USD))

      _ = whenF(itemsMock.findById(itemId1)).thenReturn(Some(item1))
      _ = whenF(itemsMock.findById(itemId2)).thenReturn(Some(item2))

      _ <- shoppingCartService.add(userId, itemId1, Quantity(2))

      response1 <- shoppingCartService.get(userId)

      _ <- expect(response1.items.exists(_.item == item1)).failFast
      _ <- expect(response1.total == Money(201.0, USD)).failFast

      _         <- shoppingCartService.update(userId, Cart(Map(itemId1 -> Quantity(1))))
      response2 <- shoppingCartService.get(userId)

      _ <- expect(response2.items.exists(_.item == item1)).failFast
      _ <- expect(response2.total == Money(100.5, USD)).failFast

      _         <- shoppingCartService.add(userId, itemId2, Quantity(1))
      _         <- shoppingCartService.removeItem(userId, itemId1)
      response3 <- shoppingCartService.get(userId)

      _ <- expect(response3.items.exists(_.item == item2)).failFast
      _ <- expect(!response3.items.exists(_.item == item1)).failFast
      _ <- expect(response3.total == Money(300.1, USD)).failFast

      _         <- shoppingCartService.delete(userId)
      response4 <- shoppingCartService.get(userId)

      _ <- expect(response4.items.isEmpty).failFast
      _ <- expect(response4.total == Money(0, USD)).failFast

    } yield expect(true)
  }

}
