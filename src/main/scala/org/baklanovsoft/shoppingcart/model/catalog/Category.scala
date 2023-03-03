package org.baklanovsoft.shoppingcart.model.catalog
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

final case class Category(uuid: CategoryId, name: CategoryName)

object Category extends RestCodecs {

  implicit val codec: Codec[Category]   = deriveCodec[Category]
  implicit val schema: Schema[Category] = Schema.derived[Category]

}
