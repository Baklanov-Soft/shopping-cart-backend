package org.baklanovsoft.shoppingcart.user.model

import enumeratum.EnumEntry.Lowercase
import enumeratum.{Enum, EnumEntry}

sealed trait Role extends EnumEntry with Lowercase

object Role extends Enum[Role] {

  case object Admin extends Role
  case object User  extends Role

  override def values: IndexedSeq[Role] = findValues
}
