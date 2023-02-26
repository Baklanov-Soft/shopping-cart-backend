package org.baklanovsoft.shoppingcart.model.catalog
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.CoercibleCodecs
import sttp.tapir.Schema
import sttp.tapir.codec.newtype.TapirCodecNewType

final case class Category(uuid: CategoryId, name: CategoryName)

object Category extends TapirCodecNewType with CoercibleCodecs {

  implicit val codec: Codec[Category]   = deriveCodec[Category]
  implicit val schema: Schema[Category] = Schema.derived[Category]

}
