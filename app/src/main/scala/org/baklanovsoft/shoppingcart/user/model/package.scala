package org.baklanovsoft.shoppingcart.user

import derevo.cats.show
import derevo.derive
import io.estatico.newtype.macros.newtype

import java.util.UUID

package object model {

  @derive(show)
  @newtype case class UserId(value: UUID)
  @newtype case class Username(value: String)
  @newtype case class JwtToken(value: String)

  @newtype case class Password(value: String)
  @newtype case class Salt(base64String: String)
  @newtype case class PasswordHashed(value: String)
}
