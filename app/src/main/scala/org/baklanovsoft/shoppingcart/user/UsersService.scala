package org.baklanovsoft.shoppingcart.user
import org.baklanovsoft.shoppingcart.user.model._

trait UsersService[F[_]] {
  def find(username: Username): F[Option[UserWithPassword]]

  def create(username: Username, password: EncryptedPassword): F[UserId]

}
