package org.baklanovsoft.shoppingcart.model.user

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

case class CreateUser(username: Username, password: Password)

object CreateUser extends RestCodecs {
  implicit val codec: Codec[CreateUser]   = deriveCodec
  implicit val schema: Schema[CreateUser] = Schema.derived
}
