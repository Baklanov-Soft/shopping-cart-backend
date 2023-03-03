ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "org.baklanovsoft"

assembly / assemblyMergeStrategy := {
  // openapi docs generation
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
    MergeStrategy.singleOrError

  // deduplicate error because of logback, this will fix
  case x                                                                            =>
    MergeStrategy.first
}

// for no main manifest attribute error
assembly / mainClass := Some("org.baklanovsoft.shoppingcart.Main")

lazy val root = (project in file("."))
  .settings(
    name := "shopping-cart-backend",
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    libraryDependencies += compilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    libraryDependencies += compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
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
