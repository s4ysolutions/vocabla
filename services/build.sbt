import sbt.Keys.libraryDependencies

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "s4y.solutions"
ThisBuild / scalaVersion := "3.7.1"

val zioVersion = "2.1.19"
val zioLoggingVersion = "2.5.0"
val zioHttpVersion = "3.3.3"
val zioSchemaVersion = "1.7.3"
val zioPreludeVersion = "1.0.0-RC41"

lazy val id = (project in file("modules/infrastructure/id"))
  .settings(
    name := "id",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion
  )

lazy val mvStore = (project in file("modules/infrastructure/mv-store"))
  .settings(
    name := "mv-store",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "com.h2database" % "h2-mvstore" % "2.3.232",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )

lazy val words = (project in file("modules/features/words"))
  .dependsOn(id)
  .dependsOn(mvStore)
  .settings(
    name := "words",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )

lazy val endpointUI =
  (project in file("modules/features/endpoint-ui"))
    .dependsOn(words)
    .settings(
      name := "endpoint-ui",
      libraryDependencies += "dev.zio" %% "zio" % zioVersion,
      libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
      libraryDependencies += "dev.zio" %% "zio-http" % zioHttpVersion,
      libraryDependencies += "dev.zio" %% "zio-logging" % zioLoggingVersion,
      libraryDependencies += "com.github.ghostdogpr" %% "caliban" % "2.10.1" % Test,
      libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
    )

// libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
// libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
