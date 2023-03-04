package org.baklanovsoft.shoppingcart.catalog.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import squants.market.Money
import sttp.tapir.Schema

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

object Item extends RestCodecs {

  implicit val codec: Codec[Item]   = deriveCodec[Item]
  implicit val schema: Schema[Item] = Schema.derived[Item]

}
