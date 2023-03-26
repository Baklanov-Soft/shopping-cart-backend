package org.baklanovsoft.shoppingcart.catalog.sql

import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.catalog.sql.BrandSQL.{brandId, brandName}
import org.baklanovsoft.shoppingcart.catalog.sql.CategoriesSQL.{categoryId, categoryName}
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market.{Money, defaultMoneyContext}

object ItemSQL {

  val itemId =
    uuid.imap[ItemId](ItemId.apply)(_.value)

  val itemName =
    varchar.imap[ItemName](ItemName.apply)(_.value)

  val itemDesc =
    varchar.imap[ItemDescription](ItemDescription.apply)(_.value)

  val money: Codec[Money] =
    (numeric ~ varchar(3)).imap[Money] { case (amount, currency) =>
      Money(amount, currency)(defaultMoneyContext).toOption.get
    }(m => (m.amount, m.currency.code))

  val decoder: Decoder[Item] =
    (
      itemId ~ itemName ~ itemDesc ~ money ~ brandId ~
        brandName ~ categoryId ~ categoryName
    ).map {
      case itemId ~
          itemName ~
          itemDesc ~
          money ~
          brandId ~
          brandName ~
          categoryId ~
          categoryName =>
        Item(
          itemId,
          itemName,
          itemDesc,
          money,
          Brand(brandId, brandName),
          Category(categoryId, categoryName)
        )
    }

  private val selectFragment: Fragment[Void] =
    sql"""
         SELECT i.uuid, i.name, i.description, i.price, i.currency,
                b.uuid, b.name, c.uuid, c.name
         FROM items AS i
         INNER JOIN brands AS b ON i.brand_id = b.uuid
         INNER JOIN categories AS c ON i.category_id = c.uuid
       """

  val selectAll: Query[Void, Item] =
    selectFragment.query(decoder)

  val selectByBrand: Query[BrandName, Item] =
    (selectFragment ~> sql"WHERE b.name LIKE $brandName").query(decoder)

  val selectById: Query[ItemId, Item] =
    (selectFragment ~> sql"WHERE i.uuid = $itemId").query(decoder)

  val selectByName: Query[ItemName, Item] =
    (selectFragment ~> sql"WHERE i.name = $itemName").query(decoder)

  val insertItem: Command[ItemId ~ CreateItem] =
    sql"""
         INSERT INTO items
         VALUES (
           $itemId, $itemName, $itemDesc,
           $money, $brandId, $categoryId
         )
       """.command.contramap { case id ~ i =>
      id ~ i.name ~ i.description ~
        i.price ~ i.brandId ~ i.categoryId
    }

  final case class UpdateItemCommand(
      id: ItemId,
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brandId: BrandId,
      categoryId: CategoryId
  )

  val updateItem: Command[UpdateItemCommand] =
    sql"""
         UPDATE items
         SET name = $itemName,
         description = $itemDesc,
         price = $numeric, 
         currency = ${varchar(3)},
         brand_id = $brandId,
         category_id = $categoryId
         WHERE uuid = $itemId
       """.command.contramap { i =>
      i.name ~ i.description ~ i.price.amount ~ i.price.currency.code ~ i.brandId ~ i.categoryId ~ i.id
    }

}
