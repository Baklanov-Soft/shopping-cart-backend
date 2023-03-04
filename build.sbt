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

lazy val root = (project in file("."))
  .settings(
    name := "shopping-cart-backend-old",
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    libraryDependencies ++= Dependencies.plugins,
    libraryDependencies ++= Seq(
      Dependencies.cats,
      Dependencies.catsRetry,
      Dependencies.newtype,
      Dependencies.squants,
      Dependencies.logback,
      Dependencies.log4cats,
      Dependencies.TestDependencies.scalaTest
    ) ++ Seq(
      Dependencies.apispec,
      Dependencies.circe,
      Dependencies.http4s,
      Dependencies.tapir
    ).flatten
  )

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
    .settings(
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
        Dependencies.newtype,
        Dependencies.squants,
        Dependencies.logback,
        Dependencies.log4cats
      ) ++ Seq(
        Dependencies.apispec,
        Dependencies.circe,
        Dependencies.http4s,
        Dependencies.refined,
        Dependencies.tapir
      ).flatten
    )
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.TestDependencies.scalaTest
      )
    )
