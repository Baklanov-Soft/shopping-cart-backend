package org.baklanovsoft.shoppingcart.model.catalog

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.CoercibleCodecs
import sttp.tapir.Schema
import sttp.tapir.codec.newtype.TapirCodecNewType

final case class Brand(uuid: BrandId, name: BrandName)

object Brand extends TapirCodecNewType with CoercibleCodecs {

  implicit val codec: Codec[Brand]   = deriveCodec[Brand]
  implicit val schema: Schema[Brand] = Schema.derived[Brand]

}
