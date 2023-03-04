package org.baklanovsoft.shoppingcart.controller.v1

import cats.MonadThrow
import cats.data.EitherT
import cats.implicits._
import org.baklanovsoft.shoppingcart.controller.v1.ControllerDomain.Base64Username
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.Base64
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

final case class UserController[F[_]: MonadThrow: Base64](auth: Auth[F]) extends Controller[F] {

  private val check =
    UserController.check.serverLogic { strBase64 =>
      {
        for {
          username <- EitherT(Base64[F].decode(strBase64.value.toString()).attempt).leftMap(_ => StatusCode.BadRequest)
          code     <- EitherT.liftF[F, StatusCode, StatusCode](auth.check(Username(username)))
        } yield code
      }.value
    }

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
      .in(query[Base64Username]("Base64 username"))
      .out(statusCode)
      .errorOut(statusCode)
      .errorOut(statusCode(StatusCode.NotFound).description("No such user found by username"))
      .errorOut(statusCode(StatusCode.BadRequest).description("Base64 decoding error"))
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
