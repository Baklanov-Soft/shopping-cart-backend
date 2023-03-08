package org.baklanovsoft.shoppingcart.user.model

case class UserWithPassword(
    id: UserId,
    name: Username,
    password: HashedPassword
)
