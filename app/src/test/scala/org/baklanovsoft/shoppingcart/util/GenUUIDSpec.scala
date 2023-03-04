package org.baklanovsoft.shoppingcart.util

import cats.effect.IO
import weaver.SimpleIOSuite

object GenUUIDSpec extends SimpleIOSuite {

  private val instance = GenUUID.forSync[IO]

  test("generate uuid") {
    for {
      uuid <- instance.make
      _    <- instance.read(uuid.toString)
    } yield expect(true)
  }

  test("read valid uuid") {
    for {
      _ <- instance.read("c0266c97-1fbc-4fd2-85eb-35bf4dfe797b")
    } yield expect(true)
  }

  test("fail on invalid uuid") {
    for {
      error <- instance.read("c0266c97-1fbc-4fd2-85eb-35bf4dfe797b))").attempt
      msg    = error.left.map(_.toString)
    } yield expect(
      msg.isLeft &&
        msg.swap.getOrElse("err").contains("IllegalArgumentException: UUID string too large")
    )
  }

}
