package org.baklanovsoft.shoppingcart.controller.v1

import cats.MonadThrow
import cats.data.EitherT
import cats.implicits._
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import ErrorHandler._
import org.typelevel.log4cats.{Logger, LoggerFactory}

final case class UserController[F[_]: MonadThrow: Logger] private (auth: Auth[F]) extends Controller[F] {

  private val check =
    UserController.check.serverLogic { username =>
      {
        for {
          code <- EitherT.liftF[F, StatusCode, StatusCode](auth.check(username))
        } yield code
      }.value
    }

  private val login =
    UserController.login.serverLogic { loginUser =>
      withErrorHandler(auth.login(loginUser))
    }

  private val logout =
    UserController.logout
      .serverSecurityLogic(auth.authWithToken)
      .serverLogic { case (token, _) =>
        _ =>
          auth
            .logout(token)
            .map(_.map(_ => StatusCode.Ok))
      }

  private val resister =
    UserController.register
      .serverLogic { createUser =>
        withErrorHandler(auth.register(createUser))
      }

  override val routes = List(
    login,
    check,
    logout,
    resister
  )
}

object UserController extends RestCodecs {

  def make[F[_]: MonadThrow: LoggerFactory](auth: Auth[F]) = {
    implicit val l = LoggerFactory.getLogger[F]
    UserController[F](auth)
  }

  private val tag  = "Auth"
  private val base = Routes.base / "auth"

  private val check =
    endpoint.get
      .in(base / "check")
      .in(query[Username]("Username to check"))
      .out(statusCode)
      .errorOut(statusCode)
      .errorOut(statusCode(StatusCode.NotFound).description("No such user found by username"))
      .tag(tag)
      .summary("Check user exists")

  private val login =
    endpoint.post
      .in(base / "login")
      .in(jsonBody[LoginUser])
      .out(jsonBody[JwtToken])
      .errorOut(statusCode)
      .errorOut(plainBody[String])
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
      .errorOut(statusCode)
      .errorOut(plainBody[String])
      .tag(tag)
      .summary("Register a new user")

}
