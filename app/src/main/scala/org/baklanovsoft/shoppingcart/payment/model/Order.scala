package org.baklanovsoft.shoppingcart.payment.model

import derevo.circe._
import derevo.derive
import org.baklanovsoft.shoppingcart.catalog.model.{ItemId, Quantity}
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import squants.market.Money
import sttp.tapir.Schema
import sttp.tapir.derevo._

@derive(codec, schema)
case class Order(
    id: OrderId,
    pid: PaymentId,
    items: Map[ItemId, Quantity],
    total: Money
)

object Order {
  implicit private val cartMapSchema: Schema[Map[ItemId, Quantity]] =
    Schema.schemaForMap[ItemId, Quantity](_.value.toString)
}
