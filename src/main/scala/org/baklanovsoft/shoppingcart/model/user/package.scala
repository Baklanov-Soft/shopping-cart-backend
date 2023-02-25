package org.baklanovsoft.shoppingcart.model

import io.estatico.newtype.macros.newtype

import java.util.UUID

package object user {

  @newtype case class UserId(value: UUID)
  @newtype case class Username(value: String)
  @newtype case class Password(value: String)
  @newtype case class EncryptedPassword(value: String)
  @newtype case class JwtToken(value: String)
}
