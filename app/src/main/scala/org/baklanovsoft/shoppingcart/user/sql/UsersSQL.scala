package org.baklanovsoft.shoppingcart.user.sql

import org.baklanovsoft.shoppingcart.user.model.{PasswordHashed, Role, Salt, UserId, Username}
import skunk._
import skunk.codec.all._
import skunk.implicits._

object UsersSQL {

  final case class SaltDb(userId: UserId, salt: Salt, iterations: Int)
  final case class UserDb(userId: UserId, username: Username, passwordHashed: PasswordHashed)

  private val role =
    varchar.imap[Role](Role.withName)(_.entryName)

  val userId: Codec[UserId] =
    uuid.imap[UserId](UserId.apply)(_.value)

  private val username =
    varchar.imap[Username](Username.apply)(_.value)

  private val passwordHashed =
    varchar.imap[PasswordHashed](PasswordHashed.apply)(_.value)

  private val salt =
    varchar.imap[Salt](Salt.apply)(_.base64String)

  private val userDb: Codec[UserDb] =
    (userId ~ username ~ passwordHashed)
      .imap[UserDb] { case i ~ n ~ p => UserDb.apply(i, n, p) }(u => u.userId ~ u.username ~ u.passwordHashed)

  private val saltDb: Codec[SaltDb] =
    (userId ~ salt ~ int4).imap { case userId ~ salt ~ iterations =>
      SaltDb(userId, salt, iterations)
    }(s => s.userId ~ s.salt ~ s.iterations)

  val selectUser: Query[Username, UserDb] =
    sql"""
         SELECT * FROM users
         WHERE username = $username
       """.query(userDb)

  val insertUser: Command[UserDb] =
    sql"""
         INSERT INTO users
         VALUES ( $userDb )
       """.command

  val updatePassword: Command[PasswordHashed ~ Username] =
    sql"""
         UPDATE users
         SET passwordHashed = $passwordHashed
         WHERE username = $username
       """.command

  val selectSalt: Query[UserId, SaltDb] =
    sql"""
         SELECT * FROM salt
         WHERE user_uuid = $userId
       """.query(saltDb)

  val upsertSalt: Command[SaltDb] =
    sql"""
         INSERT INTO salt
         VALUES( $saltDb )
         ON CONFLICT (user_uuid)
         DO UPDATE SET salt = $salt, iterations = $int4
       """.command.contramap { case saltDb =>
      saltDb ~ saltDb.salt ~ saltDb.iterations
    }

  val selectRoles: Query[UserId, Role] =
    sql"""
         SELECT role FROM roles
         WHERE user_uuid = $userId
       """.query(role)

  val addRole: Command[UserId ~ Role] =
    sql"""
         INSERT INTO roles
         VALUES ($userId, $role)
       """.command

  val removeRole: Command[UserId ~ Role] =
    sql"""
        DELETE FROM roles
        WHERE user_uuid = $userId AND role = $role
       """.command

}
