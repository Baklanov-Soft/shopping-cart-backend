package org.baklanovsoft.shoppingcart.payment.model

import org.baklanovsoft.shoppingcart.user.model.UserId
import squants.market.Money

case class Payment(
    id: UserId,
    total: Money,
    card: Card
)
