package org.baklanovsoft.shoppingcart.model.catalog

import squants.market.Money

final case class CartTotal(items: List[CartItem], total: Money)
