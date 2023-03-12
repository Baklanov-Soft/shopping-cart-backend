package org.baklanovsoft.shoppingcart

import cats.effect.IO
import cats.effect.kernel.Resource
import skunk.Session

object ResourcesRegistry {

  type Postgres = Resource[IO, Session[IO]]
}
