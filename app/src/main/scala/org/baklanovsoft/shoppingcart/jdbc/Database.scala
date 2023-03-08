package org.baklanovsoft.shoppingcart.jdbc

import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import fly4s.core.Fly4s
import fly4s.core.data.{Fly4sConfig, Location}
import natchez.Trace.Implicits.noop
import org.baklanovsoft.shoppingcart.config.DatabaseConfig
import org.typelevel.log4cats.{Logger, LoggerFactory}
import retry.RetryPolicies._
import retry.{RetryDetails, retryingOnAllErrors}
import skunk.Session
import skunk.codec.all.text
import skunk.implicits._

import scala.concurrent.duration._

final case class Database[F[_]: Async: Console: Logger] private (dbConfig: DatabaseConfig) {
  import org.baklanovsoft.shoppingcart.jdbc.Database.Pool

  private val flywayUrl = s"jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.database}"

  private val policy = limitRetries[F](5) |+| exponentialBackoff[F](5.seconds)

  private def onError(e: Throwable, d: RetryDetails) =
    Logger[F].warn(s"Migration failed with error ${e.getMessage} \nRetry details: $d")

  private val migration: F[Unit] =
    Fly4s
      .make[F](
        url = flywayUrl,
        user = Some(dbConfig.user),
        password = Some(dbConfig.password.toCharArray),
        config = Fly4sConfig(
          table = "flyway",
          locations = Location.one("migrations")
        )
      )
      .onFinalize(Logger[F].info("Fly4s connection resource is closed"))
      // to close the connection right after migration, not after app completes
      .use(r => retryingOnAllErrors.apply(policy, onError)(r.migrate))
      .void

  private def checkPostgresConnection(
      sessionR: Resource[F, Session[F]]
  ) =
    sessionR.use { session =>
      session
        .unique[String](sql"select version();".query(text))
        .flatMap { v =>
          Logger[F].info(s"Skunk connected to the Postgres: $v")
        }
    }

  private val sessionPool: Pool[F] =
    Session
      .pooled[F](
        host = dbConfig.host,
        port = dbConfig.port,
        user = dbConfig.user,
        password = Some(dbConfig.password),
        database = dbConfig.database,
        max = 10
      )
      .evalTap(checkPostgresConnection) // allows to check connection

  private def migrateAndConnect: Pool[F] = for {
    _    <- Resource.eval(Async[F].whenA(dbConfig.migrateOnStart)(migration))
    pool <- sessionPool
  } yield pool
}

object Database {
  // first resource is pool and second is separate connections
  type Pool[F[_]] = Resource[F, Resource[F, Session[F]]]

  def make[F[_]: Async: Console: LoggerFactory](dbConfig: DatabaseConfig) = {
    implicit val l = LoggerFactory[F].getLogger

    Database[F](dbConfig).migrateAndConnect
  }
}
