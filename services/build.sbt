import sbt.Keys.libraryDependencies
import sbtassembly.AssemblyPlugin

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "s4y.solutions"
ThisBuild / scalaVersion := "3.7.2"

val zioVersion = "2.1.20"
val zioConfigVersion = "4.0.4"
val zioHttpVersion = "3.4.0"
val zioLoggingVersion = "2.5.1"
val zioPreludeVersion = "1.0.0-RC41"
val zioSchemaVersion = "1.7.4"
val dotenvVersion = "5.2.2"
val munitVersion = "1.1.1"

ThisBuild / assemblyMergeStrategy := {
  case x if x.endsWith("module-info.class") => sbtassembly.MergeStrategy.discard
  case x if x.endsWith("io.netty.versions.properties") =>
    sbtassembly.MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

Test / testOptions += Tests.Argument("-v")
/*
**************** effect free pure modules *****************
 */

lazy val i18n = (project in file("modules/i18n"))
  .settings(
    name := "i18n",
    libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "dev.zio" %% "zio-schema" % zioSchemaVersion,
    libraryDependencies += "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion,
    libraryDependencies += "org.scalameta" %% "munit" % munitVersion % Test
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

/*
**************** zio effect dependent modules *****************
 */
lazy val zio = (project in file("modules/zio"))
  .dependsOn(i18n)
  .settings(
    name := "zio",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-logging" % zioLoggingVersion,
    libraryDependencies += "dev.zio" %% "zio-logging-slf4j2-bridge" % "2.5.1",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test
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

lazy val pgSQL = (project in file("modules/infra-pgsql"))
  .dependsOn(zio)
  .dependsOn(i18n)
  .dependsOn(appRepos) // for sake of errors expected by app layer
  .settings(
    name := "infra-pgsql",
    Test / parallelExecution := false,
    libraryDependencies += "org.postgresql" % "postgresql" % "42.7.7",
    libraryDependencies += "dev.zio" %% "zio-config" % zioConfigVersion,
    libraryDependencies += "com.zaxxer" % "HikariCP" % "7.0.2",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    libraryDependencies += "io.github.cdimascio" % "java-dotenv" % dotenvVersion % Test
  )

lazy val pgSqlVocabla = (project in file("modules/infra-pgsql-vocabla"))
  .dependsOn(zio)
  .dependsOn(pgSQL)
  .dependsOn(appRepos)
  .dependsOn(domain)
  .settings(
    name := "infra-pgsql-vocabla",
    parallelExecution := false,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    libraryDependencies += "io.github.cdimascio" % "java-dotenv" % dotenvVersion % Test
  )

lazy val lang = (project in file("modules/infra-lang"))
  .dependsOn(app)
  .settings(
    name := "infra-lang"
  )

lazy val app = (project in file("modules/app"))
  .dependsOn(zio)
  .dependsOn(appPorts)
  .dependsOn(appRepos)
  .dependsOn(domain)
  .settings(
    name := "app"
  )
/* unsupported storage - for future use
lazy val id = (project in file("modules/infra/id"))
  .settings(
    name := "id",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion
  )
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

lazy val rest =
  (project in file("modules/presentation-rest"))
    .dependsOn(i18n)
    .dependsOn(zio)
    .dependsOn(appPorts)
    .dependsOn(lang)
    .settings(
      name := "presentation-rest",
      libraryDependencies += "dev.zio" %% "zio-config" % zioConfigVersion,
      libraryDependencies += "dev.zio" %% "zio-http" % zioHttpVersion,
      libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test,
      libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      libraryDependencies += "org.postgresql" % "postgresql" % "42.7.7" % Test
    )

lazy val cliRest =
  (project in file("modules/cli-rest"))
    .enablePlugins(AssemblyPlugin)
    .dependsOn(rest)
    .dependsOn(app)
    .dependsOn(pgSqlVocabla)
    .settings(
      name := "cli-rest",
      Compile / mainClass := Some("solutions.s4y.vocabla.Main"),
      assembly / mainClass := Some("solutions.s4y.vocabla.Main")
    )
