package org.baklanovsoft.shoppingcart.user

import cats.effect.{Ref, Sync}
import cats.implicits._
import org.baklanovsoft.shoppingcart.error.DomainError
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.{Base64, GenUUID, Hash}
import org.typelevel.log4cats.{Logger, LoggerFactory}

trait AuthService[F[_]] {
  def findUser(token: JwtToken): F[Option[AuthUser]]
  def newUser(createUser: CreateUser): F[JwtToken]
  def check(username: Username): F[Boolean]
  def login(loginUser: LoginUser): F[JwtToken]
  def logout(token: JwtToken): F[Unit]
}

object AuthService {

  object AuthErrors {
    case object UserNotFound extends DomainError {
      val code = "UserNotFound"; val status = 404; val description = None
    }

    case object IncorrectPassword extends DomainError {
      val code = "IncorrectPassword"; val status = 403; val description = None
    }
  }

  def make[F[_]: Sync: LoggerFactory: Base64: Hash](usersService: UsersService[F]): F[AuthService[F]] =
    Ref.of[F, Map[JwtToken, AuthUser]](Map.empty).map { sessions =>
      implicit val logger: Logger[F] = LoggerFactory[F].getLogger

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

            // TODO generate normal jwt tokens
            t     <- GenUUID[F].make.map(_.toString)
            token <- Base64[F].encode(t).map(JwtToken.apply)

            _ <- sessions.update(_.updated(token, user))
            _ <- sessions.get.flatMap(s => Logger[F].info(s"New login, sessions list: $s"))
          } yield token

        override def logout(token: JwtToken): F[Unit] =
          sessions.update(_.view.filterKeys(_ != token).toMap) >>
            sessions.get.flatMap(s => Logger[F].info(s"User logout, sessions list: $s"))
      }
    }
}
