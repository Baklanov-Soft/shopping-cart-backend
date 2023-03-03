package org.baklanovsoft.shoppingcart.util
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import squants.market.{EUR, Money}
import io.circe.syntax._
import org.baklanovsoft.shoppingcart.util.rest.SquantsCodec

class SquantsCodecSpec extends AnyWordSpec with Matchers with SquantsCodec {
  "Squants money should be encoded and decoded correctly" in {
    val expected = Money.apply(100.5, EUR)

    val write = expected.asJson

    val read = write.as[Money].toOption.get

    assert(expected == read)

  }

}
