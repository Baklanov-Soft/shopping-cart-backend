package org.baklanovsoft.shoppingcart.redis

import cats.effect.std.Console
import cats.effect.{IO, Resource}
import com.redis.testcontainers.RedisContainer
import org.baklanovsoft.shoppingcart.ResourcesRegistry.{Redis => RedisT}
import org.baklanovsoft.shoppingcart.config.RedisConfig
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import weaver.{GlobalResource, GlobalWrite, LowPriorityImplicits}

object SharedRedisContainer extends GlobalResource with LowPriorityImplicits {

  private val containerR =
    Resource.make(IO(new RedisContainer(DockerImageName.parse("redis:alpine"))).flatMap { c =>
      IO(c.start()) >>
        Console[IO].println(s"Started redis container ${c.getHost}") >>
        IO.pure(c)
    }) { c =>
      Console[IO].println(s"Closing redis container ${c.getHost}") >>
        IO(c.stop())
    }

  override def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for {
      container <- containerR

      redisConfig = RedisConfig(
                      url = container.getRedisURI
                    )

      redis <- Redis.make[IO](redisConfig)

      _ <- global.putR[RedisT](redis)(classBasedInstance)
    } yield ()
}
