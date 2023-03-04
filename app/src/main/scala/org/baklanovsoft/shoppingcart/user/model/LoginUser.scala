package org.baklanovsoft.shoppingcart.user.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

case class LoginUser(username: Username, password: Password)

object LoginUser extends RestCodecs {
  implicit val codec: Codec[LoginUser]   = deriveCodec
  implicit val schema: Schema[LoginUser] = Schema.derived
}
