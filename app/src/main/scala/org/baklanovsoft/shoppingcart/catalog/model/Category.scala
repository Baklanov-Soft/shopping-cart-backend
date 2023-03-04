package org.baklanovsoft.shoppingcart.catalog.model

import derevo.circe._
import derevo.derive
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import sttp.tapir.derevo._

@derive(codec, schema)
final case class Category(uuid: CategoryId, name: CategoryName)
