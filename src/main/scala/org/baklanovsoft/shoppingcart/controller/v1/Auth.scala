package org.baklanovsoft.shoppingcart.controller.v1

import cats.Functor
import cats.implicits._
import org.baklanovsoft.shoppingcart.model.user.{JwtToken, User}
import org.baklanovsoft.shoppingcart.service.user.AuthService
import sttp.model.StatusCode

final case class Auth[F[_]: Functor](
    authService: AuthService[F]
) {

  def auth(jwtToken: JwtToken): F[Either[(StatusCode, String), User]] =
    authService
      .findUser(jwtToken)
      .map(_.fold((StatusCode.Forbidden -> "Forbidden").asLeft[User])(_.asRight))

}
