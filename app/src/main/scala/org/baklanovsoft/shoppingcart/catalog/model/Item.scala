package org.baklanovsoft.shoppingcart.catalog.model

import derevo.circe._
import derevo.derive
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import squants.market.Money
import sttp.tapir.derevo._

@derive(codec, schema)
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
    brandId: BrandId,
    categoryId: CategoryId
)

final case class UpdateItem(
    id: ItemId,
    price: Money
)
