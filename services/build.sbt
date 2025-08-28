import sbt.Keys.libraryDependencies

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "s4y.solutions"
ThisBuild / scalaVersion := "3.7.2"

val zioVersion = "2.1.20"
val zioConfigVersion = "4.0.4"
val zioHttpVersion = "3.3.3"
val zioLoggingVersion = "2.5.1"
val zioPreludeVersion = "1.0.0-RC41"
val zioSchemaVersion = "1.7.4"
val dotenvVersion = "5.2.2"
val munitVersion = "1.1.1"

Test / testOptions += Tests.Argument("-v")

lazy val i18n = (project in file("modules/i18n"))
  .settings(
    name := "i18n",
    libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "org.scalameta" %% "munit" % munitVersion % Test
  )

lazy val zio = (project in file("modules/zio"))
  .settings(
    name := "zio",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-logging" % zioLoggingVersion,
    libraryDependencies += "dev.zio" %% "zio-logging-slf4j2-bridge" % "2.5.1",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val domain = (project in file("modules/domain"))
  .dependsOn(i18n)
  .settings(
    name := "domain",
    libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion,
    libraryDependencies += "org.scalameta" %% "munit" % munitVersion % Test
  )

lazy val appRepos = (project in file("modules/app-repos"))
  .dependsOn(domain)
  .settings(
    name := "app-repos",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val appPorts = (project in file("modules/app-ports"))
  .dependsOn(domain)
  .settings(
    name := "app-ports",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

lazy val pgSQL = (project in file("modules/infra/pgsql"))
  .dependsOn(zio)
  .dependsOn(appRepos)
  .dependsOn(domain)
  .settings(
    name := "pgsql",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.7.7",
    libraryDependencies += "dev.zio" %% "zio-config" % zioConfigVersion,
    libraryDependencies += "com.zaxxer" % "HikariCP" % "7.0.2",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    libraryDependencies += "io.github.cdimascio" % "java-dotenv" % dotenvVersion % Test
  )

lazy val app = (project in file("modules/app"))
  .dependsOn(appPorts)
  .dependsOn(appRepos)
  .dependsOn(domain)
  .dependsOn(pgSQL)
  .settings(
    name := "app"
  )

lazy val id = (project in file("modules/infra/id"))
  .settings(
    name := "id",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion
  )
/*
lazy val mvStore = (project in file("modules/infra/mv-store"))
  .dependsOn(zio)
  .dependsOn(id)
  .dependsOn(appRepos)
  .dependsOn(domain)
  .settings(
    name := "mv-store",
    libraryDependencies += "com.h2database" % "h2-mvstore" % "2.3.232",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )
 */
lazy val lang = (project in file("modules/infra/lang"))
  .dependsOn(app)
  .settings(
    name := "lang"
  )

lazy val rest =
  (project in file("modules/presentation/rest"))
    .dependsOn(zio)
    .dependsOn(appPorts)
    .dependsOn(pgSQL)
    .dependsOn(lang)
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
