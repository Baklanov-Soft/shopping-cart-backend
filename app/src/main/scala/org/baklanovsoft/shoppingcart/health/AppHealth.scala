package org.baklanovsoft.shoppingcart.health

import derevo.circe._
import derevo.derive
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import sttp.tapir.derevo._

@derive(codec, schema)
final case class AppHealth(
    redis: RedisStatus,
    postgres: PostgresStatus
)
