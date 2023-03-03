package org.baklanovsoft.shoppingcart.controller.v1

import cats.Functor
import cats.implicits._
import org.baklanovsoft.shoppingcart.model.user.{CreateUser, JwtToken, LoginUser, User, Username}
import org.baklanovsoft.shoppingcart.service.user.AuthService
import sttp.model.StatusCode

final case class Auth[F[_]: Functor](
    authService: AuthService[F]
) {

  def authWithStatusAndToken(jwtToken: JwtToken): F[Either[(StatusCode, String), (JwtToken, User)]] =
    authService
      .findUser(jwtToken)
      .map(_.fold((StatusCode.Forbidden -> "Forbidden").asLeft[(JwtToken, User)])(user => (jwtToken, user).asRight))

  def authWithStatus(jwtToken: JwtToken): F[Either[(StatusCode, String), User]] =
    authWithStatusAndToken(jwtToken)
      .map(_.map(_._2))

  def auth(jwtToken: JwtToken): F[Either[Unit, User]] =
    authWithStatus(jwtToken)
      .map(_.left.map(_ => ()))

  def authWithToken(jwtToken: JwtToken): F[Either[Unit, (JwtToken, User)]] =
    authWithStatusAndToken(jwtToken)
      .map(_.left.map(_ => ()))

  def login(loginUser: LoginUser): F[Either[Unit, JwtToken]] =
    authService
      .login(loginUser.username, loginUser.password)
      .map(_.asRight[Unit])

  def logout(jwtToken: JwtToken, username: Username): F[Either[Unit, Unit]] =
    authService
      .logout(jwtToken, username)
      .map(_.asRight[Unit])

  def register(createUser: CreateUser): F[JwtToken] =
    authService
      .newUser(createUser.username, createUser.password)
}
