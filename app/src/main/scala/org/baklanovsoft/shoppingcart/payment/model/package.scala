package org.baklanovsoft.shoppingcart.payment

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.Size
import eu.timepit.refined.string.{MatchesRegex, ValidInt}
import io.estatico.newtype.macros.newtype

import java.util.UUID

package object model {

  @newtype case class OrderId(uuid: UUID)
  @newtype case class PaymentId(uuid: UUID)

  /* Card subtypes to not kick in remote api with some bad data */

  type Rgx                = "^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$"
  type CardNamePred       = String Refined MatchesRegex[Rgx]
  type CardNumberPred     = Long Refined Size[16]
  type CardExpirationPred = String Refined (Size[4] And ValidInt)
  type CardCVVPred        = Int Refined Size[3]

  @newtype case class CardName(value: CardNamePred)
  @newtype case class CardNumber(value: CardNumberPred)
  @newtype case class CardExpiration(value: CardExpirationPred)
  @newtype case class CardCVV(value: CardCVVPred)
}
