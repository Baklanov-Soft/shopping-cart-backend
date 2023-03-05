package org.baklanovsoft.shoppingcart.jdbc

import cats.effect.{IO, Resource}
import org.testcontainers.containers.PostgreSQLContainer
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

class MigrationsSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits {
  override type Res = PostgreSQLContainer[Nothing]

  override def sharedResource: Resource[IO, Res] =
    global.getOrFailR[Res](None)(classBasedInstance)

  test("Migrations passed") { _ =>
    IO.pure(assert(true))
  }
}
