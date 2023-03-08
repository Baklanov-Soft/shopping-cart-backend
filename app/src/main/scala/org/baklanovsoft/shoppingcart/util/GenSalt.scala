package org.baklanovsoft.shoppingcart.util
import cats.effect.kernel.Sync
import org.baklanovsoft.shoppingcart.user.model.Salt

import scala.util.Random

trait GenSalt[F[_]] {
  def make: F[Salt]
}

object GenSalt {
  private val SALT_LENGTH_RECOMMENDED = 16

  def apply[F[_]: GenSalt]: GenSalt[F] = implicitly

  implicit def forSync[F[_]: Sync]: GenSalt[F] =
    new GenSalt[F] {
      override def make: F[Salt] =
        Sync[F].delay(Salt(Random.nextString(SALT_LENGTH_RECOMMENDED)))
    }
}