package org.baklanovsoft.shoppingcart.controller.v1

import sttp.tapir.server.ServerEndpoint

trait Controller[F[_]] {

  val routes: List[ServerEndpoint[_, F]]

}
