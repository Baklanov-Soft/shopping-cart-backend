package org.baklanovsoft.shoppingcart.user

import cats.effect.{IO, Resource}
import org.baklanovsoft.shoppingcart.ResourcesRegistry.Postgres
import org.baklanovsoft.shoppingcart.user.UsersService.{RoleExists, UserNotFound, UsernameExists}
import org.baklanovsoft.shoppingcart.user.model._
import org.baklanovsoft.shoppingcart.util.Hash
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

import scala.util.Try

class UsersServiceSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits {
  override type Res = UsersService[IO]

  override def sharedResource: Resource[IO, Res] =
    global
      .getOrFailR[Postgres](None)(classBasedInstance)
      .map(r => UsersService.make[IO](r))

  test("User creation and password correctness") { usersService =>
    val username = Username("UsersServiceSpec1")
    val password = Password("P@ssw0rd")

    val changedPassword = Password("P@ssw0rd!")

    for {
      _ <- usersService.create(CreateUser(username, password))

      // user created
      user1 <- usersService.find(username).map(_.get)
      _     <- expect(user1.username == username).failFast

      // check username conflict error
      conflict <- usersService.create(CreateUser(username, password)).attempt
      _        <- expect(Try(conflict.swap.toOption.get.asInstanceOf[UsernameExists.type]).toOption.nonEmpty).failFast

      // hash correct
      expectedHash <- Hash[IO].calculate(password, user1.salt, user1.iterations)
      _            <- expect(user1.passwordHashed == expectedHash).failFast

      // password changed
      _             <- usersService.updatePassword(ChangePassword(username, changedPassword))
      user2         <- usersService.find(username).map(_.get)
      expectedHashC <- Hash[IO].calculate(changedPassword, user2.salt, user2.iterations)

      // password hash is expected
      _ <- expect(user2.passwordHashed == expectedHashC).failFast

      // salt and hash changed
      _ <- expect(user2.passwordHashed != user1.passwordHashed).failFast
      _ <- expect(user2.salt != user1.salt).failFast

    } yield assert(true)
  }

  test("UserNotFound error is thrown when updating the password") { usersService =>
    for {
      userNotFound <- usersService.updatePassword(ChangePassword(Username("sdkfoksdofkosd"), Password("123"))).attempt
      _            <- expect(Try(userNotFound.swap.toOption.get.asInstanceOf[UserNotFound.type]).toOption.nonEmpty).failFast
    } yield assert(true)
  }

  test("User add and revoke roles") { usersService =>
    val username = Username("UsersServiceSpec2")
    val password = Password("P@ssw0rd")

    for {
      _ <- usersService.create(CreateUser(username, password))

      // user created with default role
      user1 <- usersService.find(username).map(_.get)

      id = user1.userId

      _ <- expect(user1.roles == Set(Role.User)).failFast

      _     <- usersService.addRole(id, Role.Admin)
      user2 <- usersService.find(username).map(_.get)
      _     <- expect(user2.roles == Set(Role.User, Role.Admin)).failFast

      // check role exists error
      conflict <- usersService.addRole(id, Role.Admin).attempt
      _        <- expect(Try(conflict.swap.toOption.get.asInstanceOf[RoleExists.type]).toOption.nonEmpty).failFast

      _     <- usersService.removeRole(id, Role.Admin)
      _     <- usersService.removeRole(id, Role.User)
      user3 <- usersService.find(username).map(_.get)
      _     <- expect(user3.roles.isEmpty).failFast
    } yield assert(true)
  }

}
