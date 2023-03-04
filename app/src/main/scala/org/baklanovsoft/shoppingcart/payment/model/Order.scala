package org.baklanovsoft.shoppingcart.payment.model

import squants.market.Money
import derevo.circe._
import derevo.derive
import sttp.tapir.derevo._
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._

@derive(codec, schema)
case class Order(
    id: OrderId,
    pid: PaymentId,
    items: List[CartItem],
    total: Money
)
