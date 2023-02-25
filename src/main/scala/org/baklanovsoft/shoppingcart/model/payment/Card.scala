package org.baklanovsoft.shoppingcart.model.payment

case class Card(
    name: String,
    number: String,
    expiration: String,
    cvv: String
)
