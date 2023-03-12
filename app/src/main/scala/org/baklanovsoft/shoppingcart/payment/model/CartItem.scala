package org.baklanovsoft.shoppingcart.payment.model

import derevo.circe._
import derevo.derive
import sttp.tapir.derevo._
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import org.baklanovsoft.shoppingcart.catalog.model._
import squants.market.Money

@derive(codec, schema)
final case class CartItem(item: Item, quantity: Quantity) {
  def subTotal: Money = item.price * quantity.value.toDouble
}
