package org.baklanovsoft.shoppingcart.health

import cats.effect.Resource
import cats.effect.kernel.Concurrent
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import skunk.{Session, _}
import skunk.codec.all._
import skunk.implicits._

trait HealthService[F[_]] {
  def status: F[AppHealth]
}

object HealthService {

  def make[F[_]: Concurrent](sessionR: Resource[F, Session[F]], redis: RedisCommands[F, String, String]) =
    new HealthService[F] {

      private val healthSql: Query[Void, Int] =
        sql"SELECT 1".query(int4)

      private def checkRedis: F[Either[Throwable, Unit]] =
        redis.info
          .map {
            _.get("redis_version")
          }
          .void
          .attempt

      override def status: F[AppHealth] = for {
        postgres      <- sessionR.use(_.execute(healthSql)).attempt
        postgresStatus = PostgresStatus(postgres.fold[Status](_ => Status.Unreachable, _ => Status.Ok))

        redis      <- checkRedis
        redisStatus = RedisStatus(redis.fold[Status](_ => Status.Unreachable, _ => Status.Ok))
      } yield AppHealth(
        redisStatus,
        postgresStatus
      )

    }
}
