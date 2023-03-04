package org.baklanovsoft.shoppingcart.payment.model

import derevo.derive
import derevo.circe.codec
import org.baklanovsoft.shoppingcart.catalog.model.{ItemId, Quantity}
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import sttp.tapir.Schema
import sttp.tapir.derevo.schema

@derive(codec, schema)
case class Cart(items: Map[ItemId, Quantity])

object Cart {
  implicit private val cartMapSchema: Schema[Map[ItemId, Quantity]] =
    Schema.schemaForMap[ItemId, Quantity](_.value.toString)
}
