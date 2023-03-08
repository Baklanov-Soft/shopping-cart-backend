package org.baklanovsoft.shoppingcart.user.model

// model for authentication of the user
case class AuthUser(
    userId: UserId,
    username: Username,
    passwordHashed: PasswordHashed,
    salt: Salt,
    iterations: Int,
    roles: Set[Role]
)
