package org.baklanovsoft.shoppingcart.model.user

case class UserWithPassword(
    id: UserId,
    name: Username,
    password: EncryptedPassword
)
