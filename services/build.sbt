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

lazy val zio = (project in file("modules/zio"))
  .settings(
    name := "zio",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-logging" % zioLoggingVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )

lazy val id = (project in file("modules/infra/id"))
  .settings(
    name := "id",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion
  )

lazy val domain = (project in file("modules/domain"))
  .settings(
    name := "domain",
    libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion
  )

lazy val appRepos = (project in file("modules/app-repos"))
  .dependsOn(domain)
  .settings(
    name := "app-repos",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion
  )

lazy val appPorts = (project in file("modules/app-ports"))
  .dependsOn(domain)
  .settings(
    name := "app-ports",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val app = (project in file("modules/app"))
  .dependsOn(appPorts)
  .dependsOn(domain)
  .settings(
    name := "app",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val mvStore = (project in file("modules/infra/mv-store"))
  .dependsOn(zio)
  .dependsOn(id)
  .dependsOn(appRepos)
  .dependsOn(domain)
  .settings(
    name := "mv-store",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
    libraryDependencies += "com.h2database" % "h2-mvstore" % "2.3.232",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val lang = (project in file("modules/infra/lang"))
  .dependsOn(app)
  .settings(
    name := "lang"
  )

lazy val rest =
  (project in file("modules/presentation/rest"))
    .dependsOn(zio)
    .settings(
      name := "rest",
      libraryDependencies += "dev.zio" %% "zio" % zioVersion,
      libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
      libraryDependencies += "dev.zio" %% "zio-http" % zioHttpVersion,
      libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
      libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    )

// libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
// libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
