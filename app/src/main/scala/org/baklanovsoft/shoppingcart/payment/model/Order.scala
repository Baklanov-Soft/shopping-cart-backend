package org.baklanovsoft.shoppingcart.payment.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import squants.market.Money
import sttp.tapir.Schema

case class Order(
    id: OrderId,
    pid: PaymentId,
    items: List[CartItem],
    total: Money
)

object Order extends RestCodecs {
  implicit val codec: Codec[Order]   = deriveCodec
  implicit val schema: Schema[Order] = Schema.derived
}
