package solutions.s4y.infra.pgsql

import zio.Config
import zio.config.*

final case class PgSqlConfig(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String
):
  val url: String = s"jdbc:postgresql://$host:$port/$database"

object PgSqlConfig:
  val pgSqlConfig: Config[PgSqlConfig] =
    (Config.string("host").withDefault("vocabla") zip
      Config.int("port").withDefault(5432) zip
      Config.string("user").withDefault("postgres") zip
      Config.string("password") zip
      Config.string("database").withDefault("postgres"))
      .nested("pgsql")
      .to[PgSqlConfig]
