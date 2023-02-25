package org.baklanovsoft.shoppingcart.model.payment

import org.baklanovsoft.shoppingcart.model.user.UserId
import squants.market.Money

case class Payment(
    id: UserId,
    total: Money,
    card: Card
)
