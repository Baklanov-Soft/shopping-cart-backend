package org.baklanovsoft.shoppingcart.user

import cats.effect.Sync
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import org.baklanovsoft.shoppingcart.error.DomainError
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.{Base64, GenUUID, Hash}
import org.typelevel.log4cats.{Logger, LoggerFactory}
import io.circe.syntax._
import io.circe.parser

import scala.concurrent.duration._

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

  def make[F[_]: Sync: LoggerFactory: Base64: Hash](
      usersService: UsersService[F],
      redis: RedisCommands[F, String, String]
  ): AuthService[F] = {
    implicit val logger: Logger[F] = LoggerFactory[F].getLogger

    new AuthService[F] {

      private val SESSION_EXPIRATION = 3.hours

      override def findUser(token: JwtToken): F[Option[AuthUser]] =
        for {
          maybeUser <- redis
                         .get(token.value)
                         .map(_.flatMap(s => parser.parse(s).flatMap(_.as[AuthUser]).toOption))

          // update session expiration when user exists
          _         <- Sync[F].whenA(maybeUser.nonEmpty)(redis.expire(token.value, SESSION_EXPIRATION))
        } yield maybeUser

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

          _ <- redis.setEx(token.value, user.asJson.spaces2, SESSION_EXPIRATION)
          _ <- Logger[F].info(s"New login by user ${user.username}")
        } yield token

      override def logout(token: JwtToken): F[Unit] =
        redis.del(token.value).void
    }
  }
}
