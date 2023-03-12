package org.baklanovsoft.shoppingcart.http

import cats.Monad
import cats.effect._
import com.comcast.ip4s._
import org.baklanovsoft.shoppingcart.config.HttpConfig
import org.baklanovsoft.shoppingcart.controller.v1._
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.LoggerFactory
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.cors.CORSConfig.AllowedOrigin
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.interceptor.log.DefaultServerLog

import scala.concurrent.duration._

object HttpServer {

  def serverR[F[_]: Async: LoggerFactory](httpConfig: HttpConfig, routes: Routes[F]): Resource[F, Server] = {
    val logger = LoggerFactory.getLoggerFromName[F]("EmberServer")

    val logSettings =
      DefaultServerLog(
        doLogWhenReceived = s => logger.debug(s),
        doLogWhenHandled = (s, t) => t.fold(logger.debug(s))(logger.error(_)(s)),
        doLogAllDecodeFailures = (s, t) => t.fold(logger.debug(s))(logger.error(_)(s)),
        doLogExceptions = (s, t) => logger.error(t)(s),
        noLog = Monad[F].unit
      )

    val allowedOriginsConfig =
      if (httpConfig.corsAllowedOrigins.nonEmpty) {
        def matchRule(check: String): Boolean =
          httpConfig.corsAllowedOrigins.contains(check)

        val cfg = AllowedOrigin.Matching(matchRule)

        CORSConfig.default.copy(cfg)

      } else CORSConfig.default.allowAllOrigins

    val corsSettings =
      CORSInterceptor
        .customOrThrow[F](
          allowedOriginsConfig.allowAllHeaders.allowAllMethods
        )

    val options =
      Http4sServerOptions
        .customiseInterceptors[F]
        .corsInterceptor(corsSettings)
        .serverLog(logSettings)
        .options

    val router: HttpApp[F] = Router.apply[F]("/" -> routes.http4sRoutes(options)).orNotFound

    EmberServerBuilder
      .default[F]
      .withHost(Host.fromString(httpConfig.host).get)
      .withPort(Port.fromInt(httpConfig.port).get)
      .withHttpApp(router)
      .withShutdownTimeout(5.seconds)
      .build
  }

}
