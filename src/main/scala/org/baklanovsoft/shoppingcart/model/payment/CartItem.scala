package org.baklanovsoft.shoppingcart.model.payment
import org.baklanovsoft.shoppingcart.model.catalog.{Item, Quantity}

final case class CartItem(item: Item, quantity: Quantity)
