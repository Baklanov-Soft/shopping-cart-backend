package org.baklanovsoft.shoppingcart.controller.v1

import sttp.tapir.server.ServerEndpoint

trait Controller[F[_]] {

  /** Do not write explicit types in subtypes of Controller[F] since it will lead to compile error ¯\_(ツ)_/¯
    */
  val routes: List[ServerEndpoint[_, F]]

}
