package org.baklanovsoft.shoppingcart.user

import cats.implicits._
import cats.effect.Async
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.user.sql.UsersSQL.{SaltDb, UserDb}
import org.baklanovsoft.shoppingcart.util.{GenSalt, GenUUID, Hash}
import skunk.Session

import scala.annotation.nowarn
import scala.util.control.NoStackTrace

trait UsersService[F[_]] {
  def find(username: Username): F[Option[(UserDb, SaltDb)]]
  def create(createUser: CreateUser): F[UserId]
  def updatePassword(changePassword: ChangePassword): F[Unit]
}

@nowarn
object UsersService {

  case object UserNotFound extends NoStackTrace

  def make[F[_]: Async: GenUUID: GenSalt: Hash](sessionR: Resource[F, Session[F]]) = new UsersService[F] {
    import org.baklanovsoft.shoppingcart.user.sql.UsersSQL
    import org.baklanovsoft.shoppingcart.user.sql.UsersSQL._

    override def find(
        username: Username
    ): F[Option[(UserDb, SaltDb)]] =
      sessionR.use { s =>
        for {
          user      <- s.prepare(selectUser)
          maybeUser <- user.option(username)
          maybeSalt <- maybeUser.flatTraverse(u => s.prepare(selectSalt).flatMap(_.option(u.userId)))
        } yield (maybeUser, maybeSalt).tupled

      }

    override def create(
        createUser: CreateUser
    ): F[UserId] =
      sessionR.use { s =>
        for {
          uuid <- GenUUID[F].make.map(UserId.apply)
          salt <- GenSalt[F].make

          iterations = Hash.ITERATIONS_RECOMMENDED

          passwordHash <- Hash[F].calculate(createUser.password, salt, iterations)

          insertUser <- s.prepare(insertUser)
          _          <- insertUser.execute(UserDb(uuid, createUser.username, passwordHash))

          upsertSalt <- s.prepare(upsertSalt)
          _          <- upsertSalt.execute(SaltDb(uuid, salt, iterations))

        } yield uuid

      }

    override def updatePassword(
        changePassword: ChangePassword
    ): F[Unit] = sessionR.use { s =>
      for {

        salt         <- GenSalt[F].make
        iterations    = Hash.ITERATIONS_RECOMMENDED
        passwordHash <- Hash[F].calculate(changePassword.password, salt, iterations)

        selectUser <- s.prepare(selectUser)
        maybeUser  <- selectUser.option(changePassword.username)
        user       <- Async[F].fromOption(maybeUser, UserNotFound)

        updatePassword <- s.prepare(UsersSQL.updatePassword)
        _              <- updatePassword.execute(passwordHash, changePassword.username)

        updateSalt <- s.prepare(upsertSalt)
        _          <- updateSalt.execute(SaltDb(user.userId, salt, iterations))

      } yield ()

    }
  }
}
