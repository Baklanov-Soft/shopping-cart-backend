package org.baklanovsoft.shoppingcart.util

import cats.implicits._
import cats.effect.kernel.Sync
import org.baklanovsoft.shoppingcart.user.model.Salt

import java.nio.charset.StandardCharsets
import scala.util.Random

trait GenSalt[F[_]] {
  def make: F[Salt]
}

object GenSalt {
  private val SALT_LENGTH_RECOMMENDED = 16

  def apply[F[_]: GenSalt]: GenSalt[F] = implicitly

  implicit def forSync[F[_]: Sync: Base64]: GenSalt[F] =
    new GenSalt[F] {

      private def bytes =
        Random.nextString(SALT_LENGTH_RECOMMENDED).getBytes(StandardCharsets.UTF_8)

      override def make: F[Salt] =
        Base64[F].encode(bytes).map(Salt.apply)
    }
}
