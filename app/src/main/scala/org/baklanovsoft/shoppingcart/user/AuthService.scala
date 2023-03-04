package org.baklanovsoft.shoppingcart.user
import org.baklanovsoft.shoppingcart.user.model._

trait AuthService[F[_]] {
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: Username, password: Password): F[JwtToken]
  def check(username: Username): F[Boolean]
  def login(username: Username, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: Username): F[Unit]
}