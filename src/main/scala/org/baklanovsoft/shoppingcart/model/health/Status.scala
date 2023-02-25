package org.baklanovsoft.shoppingcart.model.health

sealed trait Status
object Status {
  case object Ok          extends Status
  case object Unreachable extends Status
}
