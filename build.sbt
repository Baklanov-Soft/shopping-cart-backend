ThisBuild / scalaVersion := "2.13.10"

val org = "org.baklanovsoft"

val assemblyStrategy = assembly / assemblyMergeStrategy := {
  // openapi docs generation
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
    MergeStrategy.singleOrError

  // deduplicate error because of logback, this will fix
  case x                                                                            =>
    MergeStrategy.first
}

lazy val app =
  (project in file("./app"))
    .settings(
      name         := "shopping-cart-backend",
      organization := org,
      version      := "0.1.0-SNAPSHOT"
    )
    .settings(
      assemblyStrategy,
      // for no main manifest attribute error
      assembly / mainClass := Some("org.baklanovsoft.shoppingcart.Main")
    )
    .configs(IntegrationTest)
    .settings(
      Defaults.itSettings,
      scalacOptions ++= Seq(
        "-Ymacro-annotations"
      )
    )
    .settings(
      libraryDependencies ++= Dependencies.plugins,
      libraryDependencies ++= Seq(
        Dependencies.cats,
        Dependencies.catsEffect,
        Dependencies.catsRetry,
        Dependencies.fs2,
        Dependencies.flyway,
        Dependencies.flyway4s,
        Dependencies.newtype,
        Dependencies.pbkdf2,
        Dependencies.postgresql,
        Dependencies.squants,
        Dependencies.logback,
        Dependencies.log4cats
      ) ++ Seq(
        Dependencies.apispec,
        Dependencies.circe,
        Dependencies.derevo,
        Dependencies.enumeratum,
        Dependencies.http4s,
        Dependencies.pureconfig,
        Dependencies.refined,
        Dependencies.skunk,
        Dependencies.tapir
      ).flatten
    )
    .settings(
      dependencyOverrides ++= Seq(
      ) ++ Seq(
        Dependencies.circe // conflicts with derevo-circe
      ).flatten
    )
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.TestDependencies.scalaTest
      ) ++ Seq(
        Dependencies.TestDependencies.testcontainers,
        Dependencies.TestDependencies.weaver
      ).flatten
    )
    .settings(
      testFrameworks += new TestFramework("weaver.framework.CatsEffect")
    )
    .settings(
      coverageFailOnMinimum    := true,
      coverageMinimumStmtTotal := 15
    )
