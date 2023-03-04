package org.baklanovsoft.shoppingcart.health

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

final case class AppHealth(
    redis: RedisStatus,
    postgres: PostgresStatus
)

object AppHealth extends RestCodecs {
  implicit val codec: Codec[AppHealth]   = deriveCodec[AppHealth]
  implicit val schema: Schema[AppHealth] = Schema.derived[AppHealth]
}
