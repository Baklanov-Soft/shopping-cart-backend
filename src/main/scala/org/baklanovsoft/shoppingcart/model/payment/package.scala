package org.baklanovsoft.shoppingcart.model

import io.estatico.newtype.macros.newtype

import java.util.UUID

package object payment {

  @newtype case class OrderId(uuid: UUID)

  @newtype case class PaymentId(uuid: UUID)

}
