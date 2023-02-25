package org.baklanovsoft.shoppingcart.model.payment

import squants.market.Money

final case class CartTotal(items: List[CartItem], total: Money)
