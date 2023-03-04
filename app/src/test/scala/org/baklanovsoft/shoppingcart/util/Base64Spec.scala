package org.baklanovsoft.shoppingcart.util

import cats.effect.IO
import weaver.SimpleIOSuite

object Base64Spec extends SimpleIOSuite {

  private val instance = Base64.forSync[IO]

  test("encode and decode base64") {
    for {
      base64 <- instance.encode("user:password")
      string <- instance.decode(base64)
    } yield expect(
      base64 == "dXNlcjpwYXNzd29yZA==" &&
        string == "user:password"
    )
  }

  test("fail on invalid input in base64 decode") {
    for {
      err <- instance.decode("dXNlcjpwYXNzd29yZA==123").attempt
      msg  = err.left.map(_.toString)
    } yield expect(
      msg.isLeft &&
        msg.swap.getOrElse("").contains("IllegalArgumentException: Input byte array has incorrect ending byte")
    )
  }

}
