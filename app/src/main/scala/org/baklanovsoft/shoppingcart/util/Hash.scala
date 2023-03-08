package org.baklanovsoft.shoppingcart.util

import cats.effect.kernel.Sync
import cats.implicits._
import io.github.nremond.PBKDF2
import org.baklanovsoft.shoppingcart.user.model.{Password, PasswordHashed, Salt}

import java.nio.charset.StandardCharsets

trait Hash[F[_]] {
  def calculate(password: Password, salt: Salt, iterations: Int): F[PasswordHashed]
}

object Hash {
  val ITERATIONS_RECOMMENDED: Int = 210_000 // 2023 OWASP for HmacSHA512

  def apply[F[_]: Hash]: Hash[F] = implicitly

  implicit def forSync[F[_]: Sync: Base64]: Hash[F] =
    (password: Password, salt: Salt, iterations: Int) =>
      for {
        hashBytes <- Sync[F].delay {
                       PBKDF2.apply(
                         password = password.value.getBytes(StandardCharsets.UTF_8),
                         salt = salt.value.getBytes(StandardCharsets.UTF_8),
                         iterations = iterations,
                         dkLength = 32,
                         cryptoAlgo = "HmacSHA512"
                       )
                     }
        hashStr   <- Base64[F].encode(hashBytes)
      } yield PasswordHashed(hashStr)
}
