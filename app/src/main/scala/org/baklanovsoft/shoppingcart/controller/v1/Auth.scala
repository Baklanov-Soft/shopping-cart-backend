package org.baklanovsoft.shoppingcart.controller.v1

import cats.Functor
import cats.implicits._
import org.baklanovsoft.shoppingcart.user.AuthService
import org.baklanovsoft.shoppingcart.user.model._
import sttp.model.StatusCode

final case class Auth[F[_]: Functor](
    authService: AuthService[F]
) {

  def authWithStatusAndToken(jwtToken: JwtToken): F[Either[(StatusCode, String), (JwtToken, AuthUser)]] =
    authService
      .findUser(jwtToken)
      .map(_.fold((StatusCode.Forbidden -> "Forbidden").asLeft[(JwtToken, AuthUser)])(user => (jwtToken, user).asRight))

  def authWithStatus(jwtToken: JwtToken): F[Either[(StatusCode, String), AuthUser]] =
    authWithStatusAndToken(jwtToken)
      .map(_.map(_._2))

  def auth(jwtToken: JwtToken): F[Either[Unit, AuthUser]] =
    authWithStatus(jwtToken)
      .map(_.left.map(_ => ()))

  def authWithToken(jwtToken: JwtToken): F[Either[Unit, (JwtToken, AuthUser)]] =
    authWithStatusAndToken(jwtToken)
      .map(_.left.map(_ => ()))

  def login(loginUser: LoginUser): F[Either[Unit, JwtToken]] =
    authService
      .login(loginUser)
      .map(_.asRight[Unit])

  def check(username: Username): F[StatusCode] =
    authService
      .check(username)
      .map {
        case true  => StatusCode.Ok
        case false => StatusCode.NotFound
      }

  def logout(jwtToken: JwtToken): F[Either[Unit, Unit]] =
    authService
      .logout(jwtToken)
      .map(_.asRight[Unit])

  def register(createUser: CreateUser): F[JwtToken] =
    authService
      .newUser(createUser)
}
