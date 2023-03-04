package org.baklanovsoft.shoppingcart.catalog.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

final case class Brand(uuid: BrandId, name: BrandName)

object Brand extends RestCodecs {

  implicit val codec: Codec[Brand]   = deriveCodec[Brand]
  implicit val schema: Schema[Brand] = Schema.derived[Brand]

}
