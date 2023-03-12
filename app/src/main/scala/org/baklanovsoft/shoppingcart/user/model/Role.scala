package org.baklanovsoft.shoppingcart.user.model

import enumeratum.EnumEntry.Lowercase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait Role extends EnumEntry with Lowercase

object Role extends Enum[Role] with CirceEnum[Role] {

  case object Admin extends Role
  case object User  extends Role

  override def values: IndexedSeq[Role] = findValues
}
