import sbt._

object Dependencies {

  object Versions {
    val cats       = "2.9.0"
    val catsEffect = "3.4.8"
    val catsRetry  = "3.1.0"

    val circe = "0.14.4"

    val doobie = "0.13.4"

    val flyway = "9.15.1"

    val fs2 = "3.6.1"

    val http4s = "0.23.18"

    val log4cats = "2.5.0"
    val logback  = "1.4.5"

    val newtype = "0.4.4"

    val pureconfig = "0.17.2"

    val redis4cats = "1.4.0"
    val refined    = "0.10.1"

    val scalaTest = "3.2.15"
    val scalaMock = "3.6.0"

    val tapir          = "1.2.9"
    val testcontainers = "1.17.6"

    val weaver = "0.8.1"
  }

  val cats       = "org.typelevel"    %% "cats-core"   % Versions.cats
  val catsEffect = "org.typelevel"    %% "cats-effect" % Versions.catsEffect
  val catsRetry  = "com.github.cb372" %% "cats-retry"  % Versions.catsRetry

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-refined"
  ).map(_ % Versions.circe)

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-postgres",
    "org.tpolecat" %% "doobie-hikari",
    "org.tpolecat" %% "doobie-refined"
  ).map(_ % Versions.doobie)

  val flyway = "org.flywaydb" % "flyway-core" % Versions.flyway

  val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2

  val http4s = Seq(
    "org.http4s" %% "http4s-core",
    "org.http4s" %% "http4s-server"
  ).map(_ % Versions.http4s)

  val log4cats = "org.typelevel" %% "log4cats-core"   % Versions.log4cats
  val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback

  val newtype = "io.estatico" %% "newtype" % Versions.newtype

  val pureconfig = Seq(
    "com.github.pureconfig" %% "pureconfig",
    "com.github.pureconfig" %% "pureconfig-core",
    "com.github.pureconfig" %% "pureconfig-generic",
    "com.github.pureconfig" %% "pureconfig-cats-effect"
  ).map(_ % Versions.pureconfig)

  val redis4cats = Seq(
    "dev.profunktor" %% "redis4cats-core",
    "dev.profunktor" %% "redis4cats-effects"
  ).map(_ % Versions.redis4cats)

  val refined = Seq(
    "eu.timepit" %% "refined",
    "eu.timepit" %% "refined-pureconfig"
  ).map(_ % Versions.refined)

  val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs",
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
    "com.softwaremill.sttp.tapir" %% "tapir-newtype",
    "com.softwaremill.sttp.tapir" %% "tapir-refined"
  ).map(_ % Versions.tapir)

  object TestDependencies {

    val testcontainers = "org.testcontainers" % "testcontainers" % Versions.testcontainers % Test

    val scalaTest = "org.scalatest" %% "scalatest"      % Versions.scalaTest % Test
    val scalaMock = "org.scalamock" %% "scalamock-core" % Versions.scalaMock % Test

    val weaver = Seq(
      "com.disneystreaming" %% "weaver-core",
      "com.disneystreaming" %% "weaver-cats"
    ).map(_ % Versions.weaver % Test)
  }

}
