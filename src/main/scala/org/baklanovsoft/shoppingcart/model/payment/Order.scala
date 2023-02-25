package org.baklanovsoft.shoppingcart.model.payment

import org.baklanovsoft.shoppingcart.model.catalog.Cart
import squants.market.Money

case class Order(
    id: OrderId,
    pid: PaymentId,
    items: Cart,
    total: Money
)
