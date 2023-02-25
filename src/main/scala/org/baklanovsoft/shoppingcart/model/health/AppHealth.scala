package org.baklanovsoft.shoppingcart.model.health

import io.estatico.newtype.macros.newtype
import org.baklanovsoft.shoppingcart.model.health.AppHealth.{PostgresStatus, RedisStatus}

case class AppHealth(
    redis: RedisStatus,
    postgres: PostgresStatus
)

object AppHealth {
  @newtype case class RedisStatus(value: Status)
  @newtype case class PostgresStatus(value: Status)
}
