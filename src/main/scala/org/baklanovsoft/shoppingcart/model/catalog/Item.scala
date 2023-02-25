package org.baklanovsoft.shoppingcart.model.catalog

import squants.market.Money

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
