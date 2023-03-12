package org.baklanovsoft.shoppingcart.catalog

import derevo.cats.show
import derevo.circe.magnolia._
import derevo.derive
import io.estatico.newtype.macros.newtype

import java.util.UUID

/** This object should be imported entirely to get codecs
  */
package object model {

  @newtype case class BrandId(value: UUID)
  @newtype case class BrandName(value: String)

  @newtype case class CategoryId(value: UUID)
  @newtype case class CategoryName(value: String)

  @derive(keyEncoder, keyDecoder, show)
  @newtype case class ItemId(value: UUID)

  @newtype case class ItemName(value: String)
  @newtype case class ItemDescription(value: String)

  @derive(show)
  @newtype case class Quantity(value: Int)
}
