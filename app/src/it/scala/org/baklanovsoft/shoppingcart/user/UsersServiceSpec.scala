package org.baklanovsoft.shoppingcart.user

import cats.effect.{IO, Resource}
import org.baklanovsoft.shoppingcart.user.model.{ChangePassword, CreateUser, Password, Username}
import org.baklanovsoft.shoppingcart.util.Hash
import skunk.Session
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

class UsersServiceSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits {
  override type Res = UsersService[IO]

  override def sharedResource: Resource[IO, Res] =
    global
      .getOrFailR[Resource[IO, Session[IO]]](None)(classBasedInstance)
      .map(r => UsersService.make[IO](r))

  test("User creation and password correctness") { usersService =>
    val username = Username("UsersServiceSpec1")
    val password = Password("P@ssw0rd")

    val changedPassword = Password("P@ssw0rd!")

    for {
      _ <- usersService.create(CreateUser(username, password))

      // user created
      (user, salt) <- usersService.find(username).map(_.get)
      _            <- expect(user.username == username).failFast

      // hash correct
      expectedHash <- Hash[IO].calculate(password, salt.salt, salt.iterations)
      _            <- expect(user.passwordHashed == expectedHash).failFast

      // password changed
      _              <- usersService.updatePassword(ChangePassword(username, changedPassword))
      (userC, saltC) <- usersService.find(username).map(_.get)
      expectedHashC  <- Hash[IO].calculate(changedPassword, saltC.salt, saltC.iterations)

      // password hash is expected
      _ <- expect(userC.passwordHashed == expectedHashC).failFast

      // salt and hash changed
      _ <- expect(userC.passwordHashed != user.passwordHashed).failFast
      _ <- expect(saltC.salt != salt.salt).failFast

    } yield assert(true)
  }

}
