package org.baklanovsoft.shoppingcart.controller.v1

import cats.effect.IO
import org.baklanovsoft.shoppingcart.error.DomainError
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import sttp.model.StatusCode
import weaver.SimpleIOSuite

object ErrorHandlerSpec extends SimpleIOSuite {

  test("Error handler works") {

    implicit val l = LoggerFactory.getLogger[IO]

    val testError = new DomainError {
      override val code: String                = "TestError"
      override val status: Int                 = 404
      override val description: Option[String] = Some("Test error")
    }

    def iWillFail: IO[Int] = IO.raiseError(testError)

    for {
      handled <- ErrorHandler.withErrorHandler(iWillFail)
    } yield expect(handled.swap.toOption.get == (StatusCode.NotFound, "TestError: Test error"))

  }
}
