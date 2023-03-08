package org.baklanovsoft.shoppingcart.util

import cats.effect.IO
import org.baklanovsoft.shoppingcart.user.model.Password
import weaver.SimpleIOSuite

object HashSpec extends SimpleIOSuite {

  test("Encoding same strings with same salt results with same hash") {
    val password   = Password("P@ssw0rd")
    val iterations = 12000

    for {
      salt <- GenSalt[IO].make

      hash1 <- Hash[IO].calculate(password, salt, iterations)
      hash2 <- Hash[IO].calculate(password, salt, iterations)

    } yield expect(hash1.value == hash2.value)
  }

  test("Encoding same strings with different salt results with different hash") {
    val password   = Password("P@ssw0rd")
    val iterations = 12000

    for {
      salt1 <- GenSalt[IO].make
      salt2 <- GenSalt[IO].make

      hash1 <- Hash[IO].calculate(password, salt1, iterations)
      hash2 <- Hash[IO].calculate(password, salt2, iterations)

    } yield expect(hash1.value != hash2.value)
  }

}
