package org.baklanovsoft.shoppingcart.payment.sql

import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.catalog.sql.ItemSQL.money
import org.baklanovsoft.shoppingcart.payment.model.{Order, OrderId, PaymentId}
import org.baklanovsoft.shoppingcart.user.model.UserId
import org.baklanovsoft.shoppingcart.user.sql.UsersSQL.userId
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import skunk._
import skunk.circe.codec.all.jsonb
import skunk.codec.all._
import skunk.implicits._

object OrdersSQL extends RestCodecs {

  private val orderId =
    uuid.imap[OrderId](OrderId.apply)(_.uuid)

  private val paymentId =
    uuid.imap[PaymentId](PaymentId.apply)(_.uuid)

  val decoder: Decoder[Order] =
    (orderId ~ userId ~ paymentId ~
      jsonb[Map[ItemId, Quantity]] ~ money)
      .map { case o ~ _ ~ p ~ i ~ t =>
        Order(o, p, i, t)
      }

  val encoder: Encoder[UserId ~ Order] =
    (orderId ~ userId ~ paymentId ~ jsonb[Map[ItemId, Quantity]] ~ money).contramap { case id ~ o =>
      o.id ~ id ~ o.pid ~ o.items ~ o.total
    }

  val selectByUserId: Query[UserId, Order] =
    sql"""
         SELECT * FROM orders
         WHERE user_id = $userId
         """.query(decoder)

  val selectByUserIdAndOrderId: Query[UserId ~ OrderId, Order] =
    sql"""
        SELECT * FROM orders
        WHERE user_id = $userId
        AND uuid = $orderId
       """.query(decoder)

  val insertOrder: Command[UserId ~ Order] =
    sql"""
         INSERT INTO orders
         VALUES ($encoder)
       """.command

}
