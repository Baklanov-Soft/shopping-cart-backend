package org.baklanovsoft.shoppingcart.controller.v1

import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import org.baklanovsoft.shoppingcart.model.catalog.BrandName

object ControllerDomain {

  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName =
      BrandName(value.toLowerCase.capitalize)
  }
}
