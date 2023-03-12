package org.baklanovsoft.shoppingcart.user.model

import derevo.circe.codec
import derevo.derive
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._

// model for authentication of the user
@derive(codec)
case class AuthUser(
    userId: UserId,
    username: Username,
    passwordHashed: PasswordHashed,
    salt: Salt,
    iterations: Int,
    roles: Set[Role]
)
