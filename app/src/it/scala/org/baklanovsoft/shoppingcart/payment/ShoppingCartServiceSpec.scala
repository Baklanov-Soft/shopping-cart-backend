package org.baklanovsoft.shoppingcart.payment

import cats.effect.IO
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.ResourcesRegistry.Redis
import org.baklanovsoft.shoppingcart.catalog.ItemsService
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.payment.model.Cart
import org.baklanovsoft.shoppingcart.user.model.UserId
import org.baklanovsoft.shoppingcart.util.GenUUID
import org.mockito.cats.MockitoCats
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import squants.market.{Money, USD}
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

class ShoppingCartServiceSpec(global: GlobalRead)
    extends IOSuite
    with LowPriorityImplicits
    with MockitoSugar
    with ArgumentMatchersSugar
    with MockitoCats {

  // Mockito can't live with parallelism: cannot invoke "cats.effect.IO.map(scala.Function1)" because "fa" is null
  override def maxParallelism: Int = 1

  override type Res = ShoppingCartService[IO]

  private val itemsMock = mock[ItemsService[IO]]

  override def sharedResource: Resource[IO, Res] =
    global
      .getOrFailR[Redis](None)(classBasedInstance)
      .map { redis =>
        ShoppingCartService.make[IO](itemsMock, redis)
      }

  test("Multiple users are getting only their carts") { shoppingCartService =>
    for {
      itemId1 <- GenUUID[IO].make.map(ItemId.apply)
      itemId2 <- GenUUID[IO].make.map(ItemId.apply)

      userId1 <- GenUUID[IO].make.map(UserId.apply)
      userId2 <- GenUUID[IO].make.map(UserId.apply)

      brandId    <- GenUUID[IO].make.map(BrandId.apply)
      categoryId <- GenUUID[IO].make.map(CategoryId.apply)

      item1 = Item(
                itemId1,
                ItemName("ShoppingCartServiceSpec-test-item-2"),
                ItemDescription("Test description"),
                Money(100.5, USD),
                Brand(
                  brandId,
                  BrandName("ShoppingCartServiceSpec-test-brand-2")
                ),
                Category(
                  categoryId,
                  CategoryName("ShoppingCartServiceSpec-test-cat-2")
                )
              )

      item2 = item1.copy(uuid = itemId2, price = Money(300.1, USD))

      _ = whenF(itemsMock.findById(itemId1)).thenReturn(Some(item1))
      _ = whenF(itemsMock.findById(itemId2)).thenReturn(Some(item2))

      // adding should sum the quantities if already exists

      _ <- shoppingCartService.add(userId1, itemId1, Quantity(1))
      _ <- shoppingCartService.add(userId2, itemId2, Quantity(1))
      _ <- shoppingCartService.add(userId1, itemId2, Quantity(1))

      response1 <- shoppingCartService.get(userId1)
      response2 <- shoppingCartService.get(userId2)

      _ <- expect(response1.items.length == 2).failFast
      _ <- expect(response1.items.map(_.item).contains(item1)).failFast
      _ <- expect(response1.items.map(_.item).contains(item2)).failFast

      _ <- expect(response2.items.length == 1).failFast
      _ <- expect(response2.items.map(_.item).contains(item2)).failFast

    } yield expect(true)
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

  test("Add and Update special cases") { shoppingCartService =>
    for {
      itemId1     <- GenUUID[IO].make.map(ItemId.apply)
      itemId2     <- GenUUID[IO].make.map(ItemId.apply)
      notRealItem <- GenUUID[IO].make.map(ItemId.apply)

      userId <- GenUUID[IO].make.map(UserId.apply)

      brandId    <- GenUUID[IO].make.map(BrandId.apply)
      categoryId <- GenUUID[IO].make.map(CategoryId.apply)

      item1 = Item(
                itemId1,
                ItemName("ShoppingCartServiceSpec-test-item-2"),
                ItemDescription("Test description"),
                Money(100.5, USD),
                Brand(
                  brandId,
                  BrandName("ShoppingCartServiceSpec-test-brand-2")
                ),
                Category(
                  categoryId,
                  CategoryName("ShoppingCartServiceSpec-test-cat-2")
                )
              )

      item2 = item1.copy(uuid = itemId2, price = Money(300.1, USD))

      _ = whenF(itemsMock.findById(itemId1)).thenReturn(Some(item1))
      _ = whenF(itemsMock.findById(itemId2)).thenReturn(Some(item2))
      _ = whenF(itemsMock.findById(notRealItem)).thenReturn(None)

      // adding should sum the quantities if already exists

      _ <- shoppingCartService.add(userId, itemId1, Quantity(1))
      _ <- shoppingCartService.add(userId, itemId1, Quantity(1))
      _ <- shoppingCartService.add(userId, itemId1, Quantity(0))

      response1 <- shoppingCartService.get(userId)

      _ <- expect(response1.items.length == 1).failFast
      _ <- expect(response1.items.exists(_.item == item1)).failFast
      _ <- expect(response1.total == Money(201.0, USD)).failFast

      // update should work as add if no item in the cart
      _         <- shoppingCartService.update(
                     userId,
                     Cart(Map(itemId1 -> Quantity(1), itemId2 -> Quantity(1), notRealItem -> Quantity(3)))
                   )
      response2 <- shoppingCartService.get(userId)

      _ <- expect(response2.items.length == 2).failFast
      _ <- expect(response2.items.exists(_.item == item1)).failFast
      _ <- expect(response2.items.exists(_.item == item2)).failFast
      _ <- expect(response2.total == Money(400.6, USD)).failFast

      // update should work as delete if updated without previous item
      _         <- shoppingCartService.update(
                     userId,
                     Cart(Map(itemId1 -> Quantity(1)))
                   )
      response3 <- shoppingCartService.get(userId)
      _         <- expect(response3.items.length == 1).failFast
      _         <- expect(response3.total == Money(100.5, USD)).failFast

      _         <- shoppingCartService.update(userId, Cart(Map.empty))
      response4 <- shoppingCartService.get(userId)
      _         <- expect(response4.items.isEmpty).failFast
      _         <- expect(response4.total == Money(0, USD)).failFast
    } yield expect(true)
  }

}
