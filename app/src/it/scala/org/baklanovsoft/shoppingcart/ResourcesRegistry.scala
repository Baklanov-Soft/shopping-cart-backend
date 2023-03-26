package org.baklanovsoft.shoppingcart

import cats.effect.IO
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.RedisCommands
import skunk.Session

object ResourcesRegistry {
  type Postgres = Resource[IO, Session[IO]]
  type Redis    = RedisCommands[IO, String, String]
}
