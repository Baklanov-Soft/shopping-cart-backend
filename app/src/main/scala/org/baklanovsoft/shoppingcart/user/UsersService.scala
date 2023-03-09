package org.baklanovsoft.shoppingcart.user

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.implicits._
import org.baklanovsoft.shoppingcart.error.DomainError
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.{GenSalt, GenUUID, Hash}
import skunk.Session

trait UsersService[F[_]] {
  def find(username: Username): F[Option[AuthUser]]
  def create(createUser: CreateUser): F[UserId]
  def updatePassword(changePassword: ChangePassword): F[Unit]
  def addRole(userId: UserId, role: Role): F[Unit]
  def removeRole(userId: UserId, role: Role): F[Unit]
}

object UsersService {

  case object UserNotFound extends DomainError {
    val code = "UserNotFound"; val status = 404; val description = None
  }

  case object UsernameExists extends DomainError {
    val code = "UsernameExists"; val status = 409; val description = None
  }

  case object RoleExists extends DomainError {
    val code = "RoleExists"; val status = 409; val description = None
  }

  def make[F[_]: Async: GenUUID: GenSalt: Hash](sessionR: Resource[F, Session[F]]) = new UsersService[F] {
    import org.baklanovsoft.shoppingcart.user.sql.UsersSQL
    import org.baklanovsoft.shoppingcart.user.sql.UsersSQL._

    override def find(
        username: Username
    ): F[Option[AuthUser]] =
      sessionR.use { s =>
        for {
          user       <- s.prepare(selectUser)
          maybeUser  <- user.option(username)
          maybeSalt  <- maybeUser.flatTraverse(u => s.prepare(selectSalt).flatMap(_.option(u.userId)))
          maybeRoles <- maybeUser.traverse(u => s.prepare(selectRoles).flatMap(_.stream(u.userId, 1024).compile.toList))

          result = (maybeUser, maybeSalt, maybeRoles).tupled.map { case (u, s, r) =>
                     AuthUser(
                       u.userId,
                       u.username,
                       u.passwordHashed,
                       s.salt,
                       s.iterations,
                       r.toSet
                     )
                   }
        } yield result

      }

    override def create(
        createUser: CreateUser
    ): F[UserId] =
      sessionR.use { s =>
        for {

          check <- s.prepare(selectUser).flatMap(_.option(createUser.username))
          _     <- Async[F].whenA(check.nonEmpty)(Async[F].raiseError(UsernameExists))

          uuid <- GenUUID[F].make.map(UserId.apply)
          salt <- GenSalt[F].make

          iterations = Hash.ITERATIONS_RECOMMENDED

          passwordHash <- Hash[F].calculate(createUser.password, salt, iterations)

          insertUser <- s.prepare(insertUser)
          _          <- insertUser.execute(UserDb(uuid, createUser.username, passwordHash))

          upsertSalt <- s.prepare(upsertSalt)
          _          <- upsertSalt.execute(SaltDb(uuid, salt, iterations))

          addRole <- s.prepare(UsersSQL.addRole)
          _       <- addRole.execute((uuid, Role.User))

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
        _              <- updatePassword.execute((passwordHash, changePassword.username))

        updateSalt <- s.prepare(upsertSalt)
        _          <- updateSalt.execute(SaltDb(user.userId, salt, iterations))

      } yield ()
    }

    def addRole(userId: UserId, role: Role): F[Unit] = sessionR.use { s =>
      for {
        check <- s.prepare(UsersSQL.selectRoles).flatMap(_.stream(userId, 1024).compile.toList)
        _     <- Async[F].whenA(check.contains(role))(Async[F].raiseError(RoleExists))
        _     <- s.prepare(UsersSQL.addRole).flatMap(_.execute((userId, role)))
      } yield ()
    }

    def removeRole(userId: UserId, role: Role): F[Unit] =
      sessionR.use(s => s.prepare(UsersSQL.removeRole).flatMap(_.execute((userId, role)))).void

  }
}
