package org.baklanovsoft.shoppingcart.user

import cats.effect.{Ref, Sync}
import cats.implicits._
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.{GenUUID, Hash}
import sttp.model.StatusCode

import scala.util.control.NoStackTrace

trait AuthService[F[_]] {
  def findUser(token: JwtToken): F[Option[AuthUser]]
  def newUser(createUser: CreateUser): F[JwtToken]
  def check(username: Username): F[Boolean]
  def login(loginUser: LoginUser): F[JwtToken]
  def logout(token: JwtToken): F[Unit]
}

object AuthService {

  sealed trait AuthErrors extends NoStackTrace {
    def statusCode: StatusCode
  }
  object AuthErrors {
    case object UserNotFound      extends AuthErrors { override val statusCode: StatusCode = StatusCode.NotFound  }
    case object IncorrectPassword extends AuthErrors { override val statusCode: StatusCode = StatusCode.Forbidden }
  }

  def make[F[_]: Sync: GenUUID: Hash](usersService: UsersService[F]) =
    Ref.of[F, Map[JwtToken, AuthUser]](Map.empty).map { sessions =>
      new AuthService[F] {

        override def findUser(token: JwtToken): F[Option[AuthUser]] =
          sessions.get.map(_.get(token))

        override def newUser(createUser: CreateUser): F[JwtToken] =
          for {
            _     <- usersService.create(createUser)
            token <- login(LoginUser(createUser.username, createUser.password))
          } yield token

        override def check(username: Username): F[Boolean] =
          usersService.find(username).map(_.nonEmpty)

        override def login(loginUser: LoginUser): F[JwtToken] =
          for {
            maybeUser   <- usersService.find(loginUser.username)
            user        <- Sync[F].fromOption(maybeUser, AuthErrors.UserNotFound)
            hashToCheck <- Hash[F].calculate(loginUser.password, user.salt, user.iterations)
            _           <- Sync[F].whenA(hashToCheck != user.passwordHashed)(Sync[F].raiseError(AuthErrors.IncorrectPassword))
            token       <- GenUUID[F].make.map(u => JwtToken(u.toString))
            _           <- sessions.update(_.updated(token, user).filterNot(_._2.userId == user.userId))
          } yield token

        override def logout(token: JwtToken): F[Unit] =
          sessions.update(_.view.filterKeys(_ != token).toMap)
      }
    }
}
