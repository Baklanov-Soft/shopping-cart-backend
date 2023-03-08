package org.baklanovsoft.shoppingcart.catalog.sql

import org.baklanovsoft.shoppingcart.catalog.model.{Brand, BrandId, BrandName}
import skunk._
import skunk.codec.all._
import skunk.implicits._

object BrandSQL {
  val brandId =
    uuid.imap[BrandId](BrandId.apply)(_.value)

  val brandName =
    varchar.imap[BrandName](BrandName.apply)(_.value)

  val codec: Codec[Brand] =
    (brandId ~ brandName).imap { case (i, n) =>
      Brand(i, n)
    }(b => b.uuid ~ b.name)

  val selectAll: Query[Void, Brand] =
    sql"""
         SELECT * FROM brands
       """.query(codec)

  val insert: Command[Brand] =
    sql"""
         INSERT INTO brands
         VALUES ($codec)
       """.command
}
