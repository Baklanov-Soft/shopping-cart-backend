package org.baklanovsoft.shoppingcart.model.payment

import squants.market.Money

case class Order(
    id: OrderId,
    pid: PaymentId,
    items: Cart,
    total: Money
)
