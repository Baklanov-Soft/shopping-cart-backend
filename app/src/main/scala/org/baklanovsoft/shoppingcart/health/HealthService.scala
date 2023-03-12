package org.baklanovsoft.shoppingcart.health

import cats.effect.Resource
import cats.effect.kernel.Concurrent
import cats.implicits._
import skunk.{Session, _}
import skunk.codec.all._
import skunk.implicits._

trait HealthService[F[_]] {
  def status: F[AppHealth]
}

object HealthService {

  def make[F[_]: Concurrent](sessionR: Resource[F, Session[F]]) =
    new HealthService[F] {

      private val healthSql: Query[Void, Int] =
        sql"SELECT 1".query(int4)

      override def status: F[AppHealth] = for {
        postgres      <- sessionR.use(_.execute(healthSql)).attempt
        postgresStatus = PostgresStatus(postgres.fold[Status](_ => Status.Unreachable, _ => Status.Ok))
      } yield AppHealth(
        RedisStatus(Status.Unreachable),
        postgresStatus
      )

    }
}
