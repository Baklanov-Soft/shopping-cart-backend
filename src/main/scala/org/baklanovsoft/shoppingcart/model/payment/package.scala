package org.baklanovsoft.shoppingcart.model

import io.estatico.newtype.macros.newtype
import org.baklanovsoft.shoppingcart.model.catalog.{ItemId, Quantity}

import java.util.UUID

package object payment {

  @newtype case class Cart(items: Map[ItemId, Quantity])
  @newtype case class OrderId(uuid: UUID)
  @newtype case class PaymentId(uuid: UUID)

}
