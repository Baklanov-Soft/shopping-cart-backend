package org.baklanovsoft.shoppingcart.catalog.sql

import org.baklanovsoft.shoppingcart.catalog.model._
import skunk._
import skunk.codec.all._
import skunk.implicits._

object CategoriesSQL {
  val categoryId =
    uuid.imap[CategoryId](CategoryId.apply)(_.value)

  val categoryName =
    varchar.imap[CategoryName](CategoryName.apply)(_.value)

  val codec: Codec[Category] =
    (categoryId ~ categoryName).imap { case (i, n) =>
      Category(i, n)
    }(b => b.uuid ~ b.name)

  val selectAll: Query[Void, Category] =
    sql"""
       SELECT * FROM categories
     """.query(codec)

  val insert: Command[Category] =
    sql"""
       INSERT INTO categories
       VALUES ($codec)
     """.command
}
