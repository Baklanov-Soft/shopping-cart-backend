package org.baklanovsoft.shoppingcart.user

import cats.Monad
import cats.implicits._
import org.baklanovsoft.shoppingcart.config.AdminConfig
import org.baklanovsoft.shoppingcart.user.model.{CreateUser, Password, Role, Username}
import org.typelevel.log4cats.LoggerFactory

object AdminInitService {

  def makeAdminUser[F[_]: Monad: LoggerFactory](adminConfig: AdminConfig, usersService: UsersService[F]): F[Unit] =
    for {
      check <- usersService.find(Username(adminConfig.name))
      _     <- Monad[F].whenA(check.isEmpty)(
                 usersService
                   .create(
                     CreateUser(Username(adminConfig.name), Password(adminConfig.password))
                   )
                   .flatMap(id => usersService.addRole(id, Role.Admin)) >>
                   LoggerFactory.getLogger[F].info(s"Created admin user with name: ${adminConfig.name}")
               )
    } yield ()

}
