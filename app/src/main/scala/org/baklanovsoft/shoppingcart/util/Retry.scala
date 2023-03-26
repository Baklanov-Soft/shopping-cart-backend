package org.baklanovsoft.shoppingcart.util

import cats.Show
import cats.effect.Temporal
import cats.implicits._
import org.typelevel.log4cats.{Logger, LoggerFactory}
import retry.{RetryDetails, RetryPolicy, retryingOnAllErrors}

sealed trait Retriable

object Retriable {
  implicit val s: Show[Retriable] = (t: Retriable) => t.toString

  case object Orders   extends Retriable
  case object Payments extends Retriable
}

/** Capability trait and a wrapper around cats-retry library to use the same retry logic in multiple places
  */
trait Retry[F[_]] {

  def retry[A](
      policy: RetryPolicy[F],
      retriable: Retriable
  )(f: F[A]): F[A]
}

object Retry {
  def apply[F[_]: Retry]: Retry[F] = implicitly

  implicit def forLoggerTemporal[F[_]: LoggerFactory: Temporal]: Retry[F] =
    new Retry[F] {
      implicit val l: Logger[F] = LoggerFactory.getLogger[F]

      override def retry[A](policy: RetryPolicy[F], retriable: Retriable)(fa: F[A]): F[A] = {
        def onError(e: Throwable, details: RetryDetails): F[Unit] =
          details match {

            case RetryDetails.WillDelayAndRetry(_, retriesSoFar, _) =>
              Logger[F].error(e)(
                s"Failed on ${retriable.show}, retried $retriesSoFar times"
              )

            case RetryDetails.GivingUp(totalRetries, _) =>
              Logger[F].error(e)(
                s"Giving up on ${retriable.show} after $totalRetries retries"
              )
          }

        retryingOnAllErrors[A](policy, onError)(fa)

      }
    }
}
