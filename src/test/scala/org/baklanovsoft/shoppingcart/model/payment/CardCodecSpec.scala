package org.baklanovsoft.shoppingcart.model.payment

import eu.timepit.refined.auto._
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.Size
import eu.timepit.refined.refineV
import eu.timepit.refined.string.ValidInt
import io.circe.syntax._
import io.circe.parser
import org.baklanovsoft.shoppingcart.util.rest.RefinedValidateSize
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CardCodecSpec extends AnyWordSpec with Matchers with RefinedValidateSize {

  "Card json codec should encode and decode successfully" in {

    /** Type aliases with predicate Size are not working so they should be written explicitly
      */
    // todo: replace with objects like object SixP extends RefinedTypeOps[Int Refined Size[6], Int]
    val cardNumber = refineV[Size[16]](1234567890123456L).toOption.get
    val cardExp    = refineV[Size[4] And ValidInt]("0101").toOption.get
    val cardCvv    = refineV[Size[3]](123).toOption.get

    val expected = Card(
      CardName("Denis"),
      CardNumber(cardNumber),
      CardExpiration(cardExp),
      CardCVV(cardCvv)
    )

    val write = expected.asJson
    val read  = write.as[Card].toOption.get

    assert(expected == read)
  }

  "Card json codec is failing" when {

    "name is invalid" in {
      val invalidName = """{
                          |  "name" : "Денис",
                          |  "number" : 1234567890123456,
                          |  "expiration" : "0101",
                          |  "cvv" : 123
                          |}""".stripMargin

      val json = parser.parse(invalidName)

      json.flatMap(_.as[Card]) should be(Symbol("left"))
    }

    "number is invalid" in {
      val invalidNumber = """{
                            |  "name" : "Denis",
                            |  "number" : 1,
                            |  "expiration" : "0101",
                            |  "cvv" : 123
                            |}""".stripMargin

      val json = parser.parse(invalidNumber)

      json.flatMap(_.as[Card]) should be(Symbol("left"))

    }

    "expiration is invalid" in {
      val invalidExpiration = """{
                                |  "name" : "Denis",
                                |  "number" : 1234567890123456,
                                |  "expiration" : "99999999",
                                |  "cvv" : 123
                                |}""".stripMargin

      val json = parser.parse(invalidExpiration)

      json.flatMap(_.as[Card]) should be(Symbol("left"))
    }

    "cvv is invalid" in {
      val invalidCvv = """{
                         |  "name" : "Denis",
                         |  "number" : 1234567890123456,
                         |  "expiration" : "0101",
                         |  "cvv" : 999999999
                         |}""".stripMargin

      val json = parser.parse(invalidCvv)

      json.flatMap(_.as[Card]) should be(Symbol("left"))

    }
  }

}
