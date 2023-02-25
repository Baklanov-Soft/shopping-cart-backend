package org.baklanovsoft.shoppingcart.model.catalog

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.syntax._

import java.util.UUID

class BrandCodecSpec extends AnyWordSpec with Matchers {

  "Brand json codec should encode and decode successfully" in {
    val expected = Brand(
      BrandId(UUID.randomUUID()),
      BrandName("Test brand")
    )

    val write = expected.asJson
    val read  = write.as[Brand].toOption.get

    assert(expected == read)
  }
}
