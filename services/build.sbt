import sbt.Keys.libraryDependencies

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.1"

val zioVersion = "2.1.19"
val zioPreludeVersion = "1.0.0-RC41"

lazy val id = (project in file("modules/infrastructure/id"))
  .settings(
    name := "id",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion
  )

lazy val kv = (project in file("modules/infrastructure/kv"))
  .settings(
    name := "kv",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )

lazy val mvStore = (project in file("modules/infrastructure/mv-store"))
  .dependsOn(kv)
  .settings(
    name := "mv-store",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "com.h2database" % "h2-mvstore" % "2.3.232",
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )

lazy val words = (project in file("modules/features/words"))
  .dependsOn(id)
  .dependsOn(kv)
  .dependsOn(mvStore)
  .settings(
    name := "words",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    // libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
    // libraryDependencies += "dev.zio" %% "zio-prelude" % zioPreludeVersion,
    libraryDependencies += "dev.zio" %% "zio-test" % zioVersion % Test
  )
/*
lazy val root = (project in file("."))
  .dependsOn(words)
  .settings(
    name := "services"
  )
 */
