package org.baklanovsoft.shoppingcart.service.user

import org.baklanovsoft.shoppingcart.model.user._

trait AuthService[F[_]] {
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: Username, password: Password): F[JwtToken]
  def login(username: Username, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: Username): F[Unit]

}
