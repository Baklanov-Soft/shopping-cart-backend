package org.baklanovsoft.shoppingcart.util.rest

import io.circe._
import squants.market.{Currency, Money, defaultMoneyContext}
import sttp.tapir.Schema

trait SquantsCodec {
  implicit val c: Codec[Money] = new Codec[Money] {
    override def apply(a: Money): Json =
      Json.fromJsonObject(
        JsonObject(
          "currency" -> Json.fromString(a.currency.code),
          "value"    -> Json.fromBigDecimal(a.amount)
        )
      )

    override def apply(c: HCursor): Decoder.Result[Money] =
      for {
        currencyS <- c.downField("currency").as[String]

        currency <- Currency
                      .apply(currencyS)(defaultMoneyContext)
                      .toEither
                      .left
                      .map(t => DecodingFailure.apply(t.getMessage, List.empty))

        value <- c.downField("value").as[BigDecimal]
      } yield Money(value, currency)
  }

  // dummy type to use it's schema as Money schema
  private case class MoneyExample(value: BigDecimal, currency: String)

  implicit val s: Schema[Money] =
    Schema.apply(Schema.derived[MoneyExample].schemaType.as[Money])

}
