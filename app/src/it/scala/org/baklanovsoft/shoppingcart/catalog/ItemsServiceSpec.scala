package org.baklanovsoft.shoppingcart.catalog

import cats.implicits._
import cats.effect.IO
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.catalog.model._
import skunk.Session
import squants.market.{Money, USD}
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

class ItemsServiceSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits {
  override type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    global.getOrFailR[Res](None)(classBasedInstance)

  test("Items service works") { pool =>
    val brands     = BrandsService.make[IO](pool)
    val categories = CategoriesService.make[IO](pool)
    val items      = ItemsService.make[IO](pool)

    val itemsGen = Range
      .inclusive(1, 10)
      .map { i =>
        val f: (BrandId, CategoryId) => CreateItem = { (b, c) =>
          CreateItem(
            name = ItemName(s"ItemsServiceSpec-item-test-${b.value.toString}-$i"),
            description = ItemDescription("test"),
            price = Money(100.5, USD),
            brandId = b,
            categoryId = c
          )
        }
        f
      }
      .toList

    val brand1name = BrandName("ItemsServiceSpec-brand-test-1")
    val brand2name = BrandName("ItemsServiceSpec-brand-test-2")
    for {
      brand1 <- brands.create(brand1name)
      brand2 <- brands.create(brand2name)

      cat <- categories.create(CategoryName("ItemsServiceSpec-cat-test-1"))

      ids1 <- itemsGen.traverse(f => items.create(f(brand1, cat)))
      ids2 <- itemsGen.traverse(f => items.create(f(brand2, cat)))

      findAll <- items.findAll

      findBrand1 <- items.findBy(brand1name)
      findBrand2 <- items.findBy(brand2name)

      byId <- (ids1 ++ ids2).traverse(items.findById)

      updatedMoney = Money(99.9, USD)
      _           <- ids1.traverse(i => items.update(UpdateItem(id = i, price = updatedMoney)))
      updated     <- ids1.traverse(items.findById)

    } yield expect( // findAll contains every item
      (ids1 ++ ids2).forall(id => findAll.exists(_.uuid == id))
    )
      .and(         // find by brands are contains only items of correct brand
        expect(
          findBrand1.map(_.uuid).intersect(ids1).length == ids1.length &&
            findBrand2.map(_.uuid).intersect(ids2).length == ids2.length &&
            findBrand1.length == itemsGen.length &&
            findBrand2.length == itemsGen.length
        )
      )
      .and(         // byId contains every created item
        expect(
          byId.flatten.length == (ids1 ++ ids2).length
        )
      )
      .and(         // price is updated
        expect(
          updated.flatten.length == ids1.length &&
            updated.flatten.map(_.price).forall(_ == updatedMoney)
        )
      )

  }

}
