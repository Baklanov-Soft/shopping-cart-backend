package org.baklanovsoft.shoppingcart.model.payment

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

case class Card(
    name: CardName,
    number: CardNumber,
    expiration: CardExpiration,
    cvv: CardCVV
)

object Card extends RestCodecs {
  implicit val codec: Codec[Card]   = deriveCodec
  implicit val schema: Schema[Card] = Schema.derived
}
