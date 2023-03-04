package org.baklanovsoft.shoppingcart.payment.model

import io.circe.parser
import org.baklanovsoft.shoppingcart.catalog.model.{ItemId, Quantity}
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.baklanovsoft.shoppingcart.util.rest.RestCodecs._
import io.circe.syntax._

import java.util.UUID

class CartCodecSpec extends AnyWordSpec with Matchers with EitherValues {

  "cart should be encoded and decoded" in {
    val m = Map(
      ItemId(UUID.fromString("c0266c97-1fbc-4fd2-85eb-35bf4dfe797b")) -> Quantity(1)
    )

    val expected = Cart(m)

    val str =
      """
        |{
        |  "items": {
        |    "c0266c97-1fbc-4fd2-85eb-35bf4dfe797b": 1
        |  }
        |}
        |""".stripMargin

    val json = parser.parse(str).value

    val write = expected.asJson

    val fromWrite = write.as[Cart].value
    val fromJson  = json.as[Cart].value

    assert(
      fromWrite == fromJson &&
        fromWrite == expected &&
        fromJson == expected &&
        json == write
    )
  }
}
