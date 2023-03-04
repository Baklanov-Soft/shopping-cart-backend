package org.baklanovsoft.shoppingcart.payment.model

import derevo.circe._
import derevo.derive
import sttp.tapir.derevo._
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._

@derive(codec, schema)
case class Card(
    name: CardName,
    number: CardNumber,
    expiration: CardExpiration,
    cvv: CardCVV
)
