package org.baklanovsoft.shoppingcart.payment.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

final case class CartItem(item: Item, quantity: Quantity)

object CartItem extends RestCodecs {
  implicit val codec: Codec[CartItem]   = deriveCodec
  implicit val schema: Schema[CartItem] = Schema.derived
}
