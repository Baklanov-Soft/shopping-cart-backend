package org.baklanovsoft.shoppingcart.catalog.sql

import org.baklanovsoft.shoppingcart.catalog.model._
import BrandSQL.{brandId, brandName}
import CategoriesSQL.{categoryId, categoryName}
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market.{Money, USD}

object ItemSQL {

  val itemId =
    uuid.imap[ItemId](ItemId.apply)(_.value)

  val itemName =
    varchar.imap[ItemName](ItemName.apply)(_.value)

  val itemDesc =
    varchar.imap[ItemDescription](ItemDescription.apply)(_.value)

  val money =
    numeric.imap[Money](v => Money(v, USD))(_.amount)

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
         SELECT i.uuid, i.name, i.description, i.price,
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

  val updateItem: Command[UpdateItem] =
    sql"""
         UPDATE items
         SET price = $money
         WHERE uuid = $itemId
       """.command.contramap { i =>
      i.price ~ i.id
    }

}
