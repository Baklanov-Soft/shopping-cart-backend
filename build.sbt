ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "org.baklanovsoft"

lazy val root = (project in file("."))
  .settings(
    name := "shopping-cart-backend",
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    libraryDependencies += Dependencies.cats,
    libraryDependencies += Dependencies.catsRetry,
    libraryDependencies += Dependencies.newtype,
    libraryDependencies += Dependencies.squants,
    libraryDependencies += Dependencies.log4cats
  )
