ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "org.baklanovsoft"

lazy val root = (project in file("."))
  .settings(
    name := "shopping-cart-backend",
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    libraryDependencies += compilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    libraryDependencies ++= Seq(
      Dependencies.cats,
      Dependencies.catsRetry,
      Dependencies.newtype,
      Dependencies.squants,
      Dependencies.log4cats,
      Dependencies.TestDependencies.scalaTest
    ) ++ Seq(
      Dependencies.circe,
      Dependencies.http4s,
      Dependencies.tapir
    ).flatten
  )
