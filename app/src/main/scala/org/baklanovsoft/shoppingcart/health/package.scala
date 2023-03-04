package org.baklanovsoft.shoppingcart

import io.estatico.newtype.macros.newtype

package object health {
  @newtype case class RedisStatus(value: Status)
  @newtype case class PostgresStatus(value: Status)
}
