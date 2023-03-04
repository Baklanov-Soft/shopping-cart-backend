package org.baklanovsoft.shoppingcart.util

import cats.implicits._
import cats.effect.Temporal
import cats.effect.std.Supervisor

import scala.concurrent.duration.FiniteDuration

/** Common effect to schedule task to run in the background in the future
  */
trait Background[F[_]] {
  def schedule[A](
      fa: F[A],
      duration: FiniteDuration
  ): F[Unit]
}

object Background {
  def apply[F[_]: Background]: Background[F] = implicitly

  /** Default instance of background
    *
    *   - Could be done just through Temporal;
    *   - restricts what to do with Temporal so more safe;
    *   - more testable.
    *
    * Supervisor is a fiber-based supervisor that monitors all fibers we started through it.
    *
    * Spawned fibers are linked to supervisor (which is fiber itself), but not to calling fiber (it will be stopped
    * after http request is done)
    */
  implicit def bgInstance[F[_]](implicit
      S: Supervisor[F],
      T: Temporal[F]
  ): Background[F] =
    new Background[F] {
      override def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
        S.supervise(T.sleep(duration) *> fa).void
    }
}
