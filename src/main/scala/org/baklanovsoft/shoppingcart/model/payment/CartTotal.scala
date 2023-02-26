package org.baklanovsoft.shoppingcart.model.payment

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import squants.market.Money
import sttp.tapir.Schema

final case class CartTotal(items: List[CartItem], total: Money)

object CartTotal extends RestCodecs {
  implicit val codec: Codec[CartTotal]   = deriveCodec
  implicit val schema: Schema[CartTotal] = Schema.derived
}
