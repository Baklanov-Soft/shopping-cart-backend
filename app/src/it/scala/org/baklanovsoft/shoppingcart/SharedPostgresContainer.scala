package org.baklanovsoft.shoppingcart

import cats.effect.std.Console
import cats.effect.{IO, Resource}
import fly4s.core.Fly4s
import fly4s.core.data.{Fly4sConfig, Location}
import org.testcontainers.containers.PostgreSQLContainer
import weaver.{GlobalResource, GlobalWrite, LowPriorityImplicits}

/** Will create Postgresql container, migrate it and then provide as shared resource
  */
object SharedPostgresContainer extends GlobalResource with LowPriorityImplicits {

  private val containerR =
    Resource.make(IO(new PostgreSQLContainer("postgres:alpine")).flatMap { c =>
      IO(c.start()) >>
        Console[IO].println(s"Started postgresql container ${c.getJdbcUrl}") >>
        IO.pure(c)
    }) { c =>
      Console[IO].println(s"Closing postgresql container ${c.getJdbcUrl}") >>
        IO(c.stop())
    }

  override def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for {
      container <- containerR
      migration  = Fly4s
                     .make[IO](
                       url = container.getJdbcUrl,
                       user = Some(container.getUsername),
                       password = Some(container.getPassword.toCharArray),
                       config = Fly4sConfig(
                         table = "flyway",
                         locations = Location.one("migrations")
                       )
                     )
                     .use(_.migrate)
      _         <- Resource.eval(migration)
      _         <- global.putR(container)(classBasedInstance)
    } yield ()
}
