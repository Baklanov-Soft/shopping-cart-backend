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

@derive(codec, schema)
final case class CreateItem(
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brandId: BrandId,
    categoryId: CategoryId
)

@derive(codec, schema)
final case class UpdateItem(
    id: ItemId,
    name: Option[ItemName] = None,
    description: Option[ItemDescription] = None,
    price: Option[Money] = None,
    brandId: Option[BrandId] = None,
    categoryId: Option[CategoryId] = None
)
