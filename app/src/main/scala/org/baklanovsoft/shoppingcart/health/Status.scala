package org.baklanovsoft.shoppingcart.health

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema

sealed trait Status
object Status {
  case object Ok          extends Status
  case object Unreachable extends Status

  implicit val codec: Codec[Status]        = deriveCodec[Status]
  implicit lazy val schema: Schema[Status] = Schema.derived[Status]
}
