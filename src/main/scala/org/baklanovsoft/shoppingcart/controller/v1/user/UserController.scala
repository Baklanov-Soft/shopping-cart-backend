package org.baklanovsoft.shoppingcart.controller.v1.user

import cats.implicits._
import cats.Functor
import org.baklanovsoft.shoppingcart.controller.v1.{Auth, Controller, Routes}
import org.baklanovsoft.shoppingcart.model.user.{CreateUser, JwtToken, LoginUser, Username}
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

final case class UserController[F[_]: Functor](auth: Auth[F]) extends Controller[F] {

  private val check =
    UserController.check.serverLogicSuccess(auth.check)

  private val login =
    UserController.login.serverLogic { loginUser =>
      auth.login(loginUser)
    }

  private val logout =
    UserController.logout
      .serverSecurityLogic(auth.authWithToken)
      .serverLogic { case (token, user) =>
        _ =>
          auth
            .logout(token, user.name)
            .map(_.map(_ => StatusCode.Ok))
      }

  private val resister =
    UserController.register
      .serverLogicSuccess { createUser =>
        auth
          .register(createUser)
          .map(t => t -> StatusCode.Ok)

      }

  override val routes = List(
    login,
    check,
    logout,
    resister
  )
}

object UserController extends RestCodecs {
  private val tag  = "Auth"
  private val base = Routes.base / "auth"

  private val check =
    endpoint.get
      .in(base / "check")
      .in(query[Username]("username"))
      .out(statusCode)
      .errorOut(statusCode(StatusCode.NotFound).description("No such user found by username"))
      .tag(tag)
      .summary("Check user exists")

  private val login =
    endpoint.post
      .in(base / "login")
      .in(jsonBody[LoginUser])
      .out(jsonBody[JwtToken])
      .tag(tag)
      .summary("Login")

  private val logout =
    Routes.secureEndpoint.post
      .in(base / "logout")
      .out(statusCode)
      .tag(tag)
      .summary("Logout")

  private val register =
    endpoint.post
      .in(base / "register")
      .in(jsonBody[CreateUser])
      .out(jsonBody[JwtToken])
      .out(statusCode)
      .tag(tag)
      .summary("Register a new user")

}
