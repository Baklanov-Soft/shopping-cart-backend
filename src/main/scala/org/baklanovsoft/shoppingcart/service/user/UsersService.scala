package org.baklanovsoft.shoppingcart.service.user

import org.baklanovsoft.shoppingcart.model.user._

trait UsersService[F[_]] {
  def find(username: Username): F[Option[UserWithPassword]]

  def create(username: Username, password: EncryptedPassword): F[UserId]

}
