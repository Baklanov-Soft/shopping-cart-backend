package org.baklanovsoft.shoppingcart.redis

import cats.MonadThrow
import cats.effect.Async
import cats.effect.kernel.Resource
import cats.implicits._
import dev.profunktor.redis4cats.effect.{Log, MkRedis}
import dev.profunktor.redis4cats.log4cats.log4CatsInstance
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import org.baklanovsoft.shoppingcart.config.RedisConfig
import org.typelevel.log4cats.{Logger, LoggerFactory}

final case class RedisConnect[F[_]: MkRedis: MonadThrow: Logger] private (cfg: RedisConfig) {

  private def checkConnection(redis: RedisCommands[F, String, String]): F[Unit] =
    redis.info.flatMap {
      _.get("redis_version").traverse_(v => Logger[F].info(s"Connected to Redis $v"))
    }

  private def connect: Resource[F, RedisCommands[F, String, String]] =
    Redis[F]
      .utf8(cfg.url)
      .evalTap(checkConnection)

}

object RedisConnect {

  def make[F[_]: LoggerFactory: Async](
      cfg: RedisConfig
  ): Resource[F, RedisCommands[F, String, String]] = {
    implicit val l: Logger[F]        = LoggerFactory.getLogger[F]
    implicit val rl: Log[F]          = log4CatsInstance[F]
    implicit val mkRedis: MkRedis[F] = MkRedis.forAsync[F]

    RedisConnect[F](cfg).connect
  }

}
