import sbt._

object Dependencies {

  object Versions {
    val apispec = "0.3.2"

    val cats       = "2.9.0"
    val catsEffect = "3.4.8"
    val catsRetry  = "3.1.0"

    val circe = "0.14.4"

    val derevo = "0.13.0"

    val enumeratum = "1.7.2"

    val flyway   = "9.15.1"
    val flyway4s = "0.0.17"

    val fs2 = "3.6.1"

    val http4s = "0.23.18"

    val log4cats = "2.5.0"
    val logback  = "1.4.5"

    val newtype = "0.4.4"

    val pbkdf2     = "0.7.0"
    val pureconfig = "0.17.2"
    val postgresql = "42.5.4"

    val redis4cats = "1.4.0"
    val refined    = "0.10.1"

    val scalaTest = "3.2.15"
    val scalaMock = "3.6.0"
    val skunk     = "0.5.1"
    val squants   = "1.8.3"

    val tapir          = "1.2.9"
    val testcontainers = "1.17.6"

    val weaver = "0.8.1"
  }

  val plugins = Seq(
    ("org.typelevel" %% "kind-projector"     % "0.13.2").cross(CrossVersion.full),
    "com.olegpy"     %% "better-monadic-for" % "0.3.1"
  ).map(compilerPlugin)

  val apispec = Seq(
    "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml"
  ).map(_ % Versions.apispec)

  val cats       = "org.typelevel"    %% "cats-core"   % Versions.cats
  val catsEffect = "org.typelevel"    %% "cats-effect" % Versions.catsEffect
  val catsRetry  = "com.github.cb372" %% "cats-retry"  % Versions.catsRetry

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-refined"
  ).map(_ % Versions.circe)

  val derevo = Seq(
    "tf.tofu" %% "derevo-core",
    "tf.tofu" %% "derevo-cats",
    "tf.tofu" %% "derevo-circe",
    "tf.tofu" %% "derevo-circe-magnolia",
    "tf.tofu" %% "derevo-pureconfig"
  ).map(_ % Versions.derevo)

  val enumeratum = Seq("com.beachape" %% "enumeratum").map(_ % Versions.enumeratum)

  val flyway   = "org.flywaydb"        % "flyway-core" % Versions.flyway
  val flyway4s = "com.github.geirolz" %% "fly4s-core"  % Versions.flyway4s

  val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2

  val http4s = Seq(
    "org.http4s" %% "http4s-core",
    "org.http4s" %% "http4s-server",
    "org.http4s" %% "http4s-ember-server"
  ).map(_ % Versions.http4s)

  val log4cats = "org.typelevel" %% "log4cats-core"   % Versions.log4cats
  val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback

  val newtype = "io.estatico" %% "newtype" % Versions.newtype

  val pbkdf2 = "io.github.nremond" %% "pbkdf2-scala" % Versions.pbkdf2

  val pureconfig = Seq(
    "com.github.pureconfig" %% "pureconfig",
    "com.github.pureconfig" %% "pureconfig-core",
    "com.github.pureconfig" %% "pureconfig-generic",
    "com.github.pureconfig" %% "pureconfig-cats-effect"
  ).map(_ % Versions.pureconfig)

  val postgresql = "org.postgresql" % "postgresql" % Versions.postgresql

  val redis4cats = Seq(
    "dev.profunktor" %% "redis4cats-core",
    "dev.profunktor" %% "redis4cats-effects",
    "dev.profunktor" %% "redis4cats-log4cats"
  ).map(_ % Versions.redis4cats)

  val refined = Seq(
    "eu.timepit" %% "refined",
    "eu.timepit" %% "refined-pureconfig"
  ).map(_ % Versions.refined)

  val skunk = Seq(
    "org.tpolecat" %% "skunk-core",
    "org.tpolecat" %% "skunk-circe"
  ).map(_ % Versions.skunk)

  val squants = "org.typelevel" %% "squants" % Versions.squants

  val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs",
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui",
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
    "com.softwaremill.sttp.tapir" %% "tapir-newtype",
    "com.softwaremill.sttp.tapir" %% "tapir-refined",
    "com.softwaremill.sttp.tapir" %% "tapir-derevo"
  ).map(_ % Versions.tapir)

  object TestDependencies {

    val testcontainers = Seq(
      "org.testcontainers" % "testcontainers",
      "org.testcontainers" % "postgresql"
    ).map(_ % Versions.testcontainers % "it")

    val scalaTest = "org.scalatest" %% "scalatest"      % Versions.scalaTest % "it,test"
    val scalaMock = "org.scalamock" %% "scalamock-core" % Versions.scalaMock % "it,test"

    val weaver = Seq(
      "com.disneystreaming" %% "weaver-core",
      "com.disneystreaming" %% "weaver-cats"
    ).map(_ % Versions.weaver % "it,test")
  }

}
