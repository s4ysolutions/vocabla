package solutions.s4y.infra.pgsql

import zio.config.*
import zio.{Config, ZIO, ZLayer}

final case class PgSqlConfig(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String,
    schema: String
):
  val url: String = s"jdbc:postgresql://$host:$port/$database"

object PgSqlConfig:
  val pgSqlConfig: Config[PgSqlConfig] =
    (Config.string("host").withDefault("vocabla") zip
      Config.int("port").withDefault(5432) zip
      Config.string("user").withDefault("postgres") zip
      Config.string("password") zip
      Config.string("database").withDefault("postgres") zip
      Config.string("schema").withDefault("vocabla"))
      .nested("pgsql")
      .to[PgSqlConfig]

  val layer: ZLayer[Any, Nothing, PgSqlConfig] = ZLayer.fromZIO(
    ZIO.config(PgSqlConfig.pgSqlConfig).orDie
  )
