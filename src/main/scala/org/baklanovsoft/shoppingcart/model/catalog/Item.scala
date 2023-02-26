package org.baklanovsoft.shoppingcart.model.catalog

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.{CoercibleCodecs, SquantsCodec}
import squants.market.Money
import sttp.tapir.Schema
import sttp.tapir.codec.newtype.TapirCodecNewType

final case class Item(
    uuid: ItemId,
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brand: Brand,
    category: Category
)

final case class CreateItem(
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brand: Brand,
    category: Category
)

final case class UpdateItem(
    uuid: ItemId,
    price: Money
)

object Item extends TapirCodecNewType with CoercibleCodecs with SquantsCodec {

  implicit val codec: Codec[Item]   = deriveCodec[Item]
  implicit val schema: Schema[Item] = Schema.derived[Item]

}
