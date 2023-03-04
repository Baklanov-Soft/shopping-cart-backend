package org.baklanovsoft.shoppingcart.user.model

import derevo.circe._
import derevo.derive
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import sttp.tapir.derevo._

@derive(codec, schema)
case class CreateUser(username: Username, password: Password)
