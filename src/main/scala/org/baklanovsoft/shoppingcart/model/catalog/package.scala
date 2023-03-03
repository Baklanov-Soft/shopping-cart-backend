package org.baklanovsoft.shoppingcart.model

import io.estatico.newtype.macros.newtype

import java.util.UUID

/** This object should be imported entirely to get codecs
  */
package object catalog {

  @newtype case class BrandId(value: UUID)
  @newtype case class BrandName(value: String)

  @newtype case class CategoryId(value: UUID)
  @newtype case class CategoryName(value: String)

  @newtype case class ItemId(value: UUID)
  @newtype case class ItemName(value: String)
  @newtype case class ItemDescription(value: String)

  @newtype case class Quantity(value: Int)

}
