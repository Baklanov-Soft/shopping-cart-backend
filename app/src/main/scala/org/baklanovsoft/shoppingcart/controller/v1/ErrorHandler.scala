package org.baklanovsoft.shoppingcart.controller.v1

import cats.implicits._
import cats.{Applicative, MonadThrow}
import org.baklanovsoft.shoppingcart.error.DomainError
import org.typelevel.log4cats.Logger
import sttp.model.StatusCode

object ErrorHandler {

  type EndpointError = (StatusCode, String)

  def withErrorHandler[F[_]: MonadThrow: Logger, A](action: F[A]): F[Either[EndpointError, A]] =
    action.attempt.flatMap(mapErrorE[F, A])

  def mapError[F[_]: Applicative: Logger]: Throwable => F[EndpointError] = {
    case d: DomainError =>
      Logger[F].warn(s"Domain error: $d") *>
        (StatusCode.apply(d.status), s"${d.code}${d.description.fold("")(s => s": $s")}".trim).pure[F]

    case e =>
      Logger[F].error(s"Unknown error: $e") *>
        (StatusCode.InternalServerError, "").pure[F]
  }

  def mapErrorE[F[_]: Applicative: Logger, A]: Either[Throwable, A] => F[Either[EndpointError, A]] = { e =>
    e.left.map(mapError[F]).leftSequence
  }
}
