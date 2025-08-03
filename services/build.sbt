import sbt.Keys.libraryDependencies

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "s4y.solutions"
ThisBuild / scalaVersion := "3.7.1"

val zioVersion = "2.1.20"
val zioLoggingVersion = "2.5.1"
val zioHttpVersion = "3.3.3"
val zioSchemaVersion = "1.7.3"
val zioPreludeVersion = "1.0.0-RC41"

Test / testOptions += Tests.Argument("-v")

lazy val identity = (project in file("modules/domain/identity"))
  .settings(
    name := "identity",
    libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion
  )

lazy val error = (project in file("modules/error"))
  .settings(
    name := "error",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )

lazy val logging = (project in file("modules/logging"))
  .settings(
    name := "logging",
    libraryDependencies += "dev.zio" %% "zio-logging" % zioLoggingVersion
  )

lazy val id = (project in file("modules/infra/id"))
  .settings(
    name := "id",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion
  )

lazy val mvStore = (project in file("modules/infra/mv-store"))
  .dependsOn(error)
  .dependsOn(logging)
  .settings(
    name := "mv-store",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
    libraryDependencies += "com.h2database" % "h2-mvstore" % "2.3.232",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )

lazy val lang = (project in file("modules/features/lang"))
  .settings(
    name := "lang"
  )

lazy val students = (project in file("modules/features/students"))
  .settings(
    name := "students"
  )

lazy val tags = (project in file("modules/features/tags"))
  .dependsOn(id)
  .dependsOn(logging)
  .dependsOn(identity)
  .dependsOn(mvStore)
  .settings(
    name := "tags",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val words = (project in file("modules/features/words"))
  .dependsOn(id)
  .dependsOn(identity)
  .dependsOn(error)
  .dependsOn(lang)
  .dependsOn(tags)
  .dependsOn(mvStore)
  .settings(
    name := "words",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val me = (project in file("modules/features/me"))
  .dependsOn(words)
  .dependsOn(tags)
  .settings(
    name := "me",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val endpointUI =
  (project in file("modules/features/endpoint-ui"))
    .dependsOn(logging)
    .dependsOn(tags)
    .dependsOn(words)
    .dependsOn(me)
    .settings(
      name := "endpoint-ui",
      libraryDependencies += "dev.zio" %% "zio" % zioVersion,
      libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
      libraryDependencies += "dev.zio" %% "zio-http" % zioHttpVersion,
      libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
      libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    )

// libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
// libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
