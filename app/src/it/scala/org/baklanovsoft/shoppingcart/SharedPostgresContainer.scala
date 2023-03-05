package org.baklanovsoft.shoppingcart

import cats.effect.std.Console
import cats.effect.{IO, Resource}
import org.baklanovsoft.shoppingcart.config.DatabaseConfig
import org.baklanovsoft.shoppingcart.jdbc.Database
import org.testcontainers.containers.PostgreSQLContainer
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
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

      dbConfig = DatabaseConfig(
                   host = container.getHost,
                   port = container.getFirstMappedPort,
                   user = container.getUsername,
                   password = container.getPassword,
                   database = container.getDatabaseName,
                   migrateOnStart = true
                 )

      migration = Database.make[IO](dbConfig)

      pool <- migration

      _ <- global.putR(pool)(classBasedInstance)
    } yield ()
}
