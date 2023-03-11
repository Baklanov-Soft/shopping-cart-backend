package org.baklanovsoft.shoppingcart.controller.v1

import cats.Functor
import cats.implicits._
import org.baklanovsoft.shoppingcart.controller.v1.ErrorHandler.EndpointError
import org.baklanovsoft.shoppingcart.user.AuthService
import org.baklanovsoft.shoppingcart.user.model._
import sttp.model.StatusCode

final case class Auth[F[_]: Functor](
    authService: AuthService[F]
) {

  private def authWithStatusAndToken(roles: Option[Set[Role]])(
      jwtToken: JwtToken
  ): F[Either[EndpointError, (JwtToken, AuthUser)]] =
    authService
      .findUser(jwtToken)
      .map { maybeUser =>
        (maybeUser, roles) match {
          // case when we have some requirements on routes
          case (Some(user), Some(roles)) =>
            if (roles.forall(required => user.roles.contains(required)))
              (jwtToken, user).asRight[EndpointError]
            else
              (StatusCode.Forbidden -> s"Not enough roles, required: $roles").asLeft

          // user is authorized, no roles requirements - just let him in
          case (Some(user), None)        =>
            (jwtToken, user).asRight[EndpointError]

          // no user found - doesn't matter if we have roles, forbidden
          case (None, _)                 =>
            (StatusCode.Forbidden -> "Forbidden").asLeft
        }

      }

  def authWithStatus(roles: Option[Set[Role]] = None)(jwtToken: JwtToken): F[Either[EndpointError, AuthUser]] =
    authWithStatusAndToken(roles)(jwtToken)
      .map(_.map(_._2))

  def authWithStatus(role: Role*)(jwtToken: JwtToken): F[Either[EndpointError, AuthUser]] =
    authWithStatus(Set.from(role).some)(jwtToken)

  /* Those methods are just checking the login with default tapir forbidden error, no roles check so no status needed */

  def auth(jwtToken: JwtToken): F[Either[Unit, AuthUser]] =
    authWithStatus(None)(jwtToken)
      .map(_.left.map(_ => ()))

  def authWithToken(jwtToken: JwtToken): F[Either[Unit, (JwtToken, AuthUser)]] =
    authWithStatusAndToken(None)(jwtToken)
      .map(_.left.map(_ => ()))

  def login(loginUser: LoginUser): F[JwtToken] =
    authService
      .login(loginUser)

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
