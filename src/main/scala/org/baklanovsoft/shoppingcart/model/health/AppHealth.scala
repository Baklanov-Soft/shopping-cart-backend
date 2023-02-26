package org.baklanovsoft.shoppingcart.model.health

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.estatico.newtype.macros.newtype
import org.baklanovsoft.shoppingcart.model.health.AppHealth.{PostgresStatus, RedisStatus}
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.tapir.Schema

final case class AppHealth(
    redis: RedisStatus,
    postgres: PostgresStatus
)

object AppHealth extends RestCodecs {
  @newtype case class RedisStatus(value: Status)
  @newtype case class PostgresStatus(value: Status)

  implicit val codec: Codec[AppHealth]   = deriveCodec[AppHealth]
  implicit val schema: Schema[AppHealth] = Schema.derived[AppHealth]
}
