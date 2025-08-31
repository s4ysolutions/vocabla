package solutions.s4y.infra.pgsql

import io.github.cdimascio.dotenv.DotenvBuilder
import solutions.s4y.infra.pgsql.PgSqlSpec.test
import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionManagerPg}
import solutions.s4y.infra.pgsql.wrappers.*
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import zio.test.{
  Spec,
  TestAspect,
  TestEnvironment,
  TestSystem,
  ZIOSpecDefault,
  assertTrue
}
import zio.{Scope, ZIO, ZLayer}

import java.sql.{Connection, DriverManager, Statement}
import java.util.Properties
import scala.util.Using

object PgSqlSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("org.postgresql\" % \"postgresql")(
      suite("Connection") {
        test("connect to test database and run simple queries") {
          for {
            connection <- ZIO.service[Connection]
            (id1, id2) <- ZIO.attempt {
              val st = connection.createStatement()
              st.executeUpdate(
                "insert into test (text) values ('example')",
                Statement.RETURN_GENERATED_KEYS
              )
              val id1 = Using.resource(st.getGeneratedKeys) { rs =>
                if (rs.next()) {
                  rs.getLong(1)
                } else {
                  throw new RuntimeException("Failed to insert record")
                }
              }
              st.executeUpdate(
                "insert into test (text) values ('example2')",
                Statement.RETURN_GENERATED_KEYS
              )
              val id2 = Using.resource(st.getGeneratedKeys) { rs =>
                if (rs.next()) {
                  rs.getLong(1)
                } else {
                  throw new RuntimeException("Failed to insert record")
                }
              }
              (id1, id2)
            }
            _ <- ZIO.log(s"Inserted record with id: $id1 and $id2")
            rs1 <- ZIO.attempt {
              val st = connection.createStatement()
              val rs = st.executeQuery("select * from test")
              var result = Seq[String]()
              while (rs.next()) {
                result = result :+ rs.getString(2)
              }
              rs.close()
              st.close()
              result
            }
            rs2 <- ZIO.attempt {
              val st =
                connection.prepareStatement(
                  "select * from test where text = ?"
                );
              st.setString(1, "example")
              val rs = st.executeQuery()
              var result = Seq[String]()
              while (rs.next()) {
                result = result :+ rs.getString(2)
              }
              rs.close()
              st.close()
              result
            }
          } yield assertTrue(
            rs1 == Seq("example", "example2"),
            rs2 == Seq("example")
          )
        }
      }.provideLayer(dataConnectionLayer),
      suite("TransactionContext")(
        test("TransactionContext is available with implicit context") {
          ZIO
            .serviceWithZIO[TransactionManagerPg] { transactionManager =>
              transactionManager.transaction[Any, Connection] {
                val ctx = summon[TransactionContextPg]
                ZIO.succeed(ctx.connection)
              }
            }
            .flatMap(connection =>
              assertTrue(
                connection.isInstanceOf[Connection]
              )
            )
        }
      ).provideLayer(transactionManagerLayer),
      suite("Wrappers")(
        test("pgWithTransaction works") {
          pgWithTransaction {
            val ctx = summon[TransactionContextPg]
            ZIO.succeed(ctx)
          }.map(connection =>
            assertTrue(connection.isInstanceOf[TransactionContextPg])
          )
        },
        test("pgUpdate") {
          pgWithTransaction {
            pgUpdate(
              "insert into test (text) values (?)",
              _.setString(1, "example3")
            )
          }.map(rowsAffected => assertTrue(rowsAffected == 1))
        },
        test("pgUpdateOne success") {
          pgWithTransaction {
            for {
              success <- pgUpdateOne(
                "insert into test (text) values (?)",
                _.setString(1, "example4")
              )
            } yield assertTrue(success)
          }
        },
        test("pgUpdateOne not found") {
          pgWithTransaction {
            for {
              found <- pgUpdateOne(
                "update test set text = ? where id = -1",
                _.setString(1, "example4")
              )
            } yield assertTrue(!found)
          }
        },
        test("pgUpdateOne multiple results") {
          pgWithTransaction {
            for {
              _ <- pgUpdateOne(
                "insert into test (text) values (?)",
                _.setString(1, "example5")
              )
              _ <- pgUpdateOne(
                "insert into test (text) values (?)",
                _.setString(1, "example5")
              )
              multiple <- pgUpdateOne(
                "update test set text = ? where text = ?",
                st => {
                  st.setString(1, "example5_updated")
                  st.setString(2, "example5")
                }
              ).either
            } yield assertTrue(
              multiple.isLeft
                && multiple.left
                  .getOrElse("")
                  .toString == """Expected exactly one row to be affected, but got 2 rows. SQL: "update test set text = ? where text = ?""""
            )
          }
        },
        test("pgSelectMany") {
          pgWithTransaction {
            for {
              _ <- pgUpdate(
                "insert into test (text) values (?)",
                _.setString(1, "example1")
              )
              _ <- pgUpdate(
                "insert into test (text) values (?)",
                _.setString(1, "example2")
              )
              results <- pgSelectMany(
                "select * from test",
                _ => (),
                rs => rs.getString("text")
              )
            } yield assertTrue(results == Seq("example1", "example2"))
          }
        },
        test("pgSelectOne success") {
          pgWithTransaction {
            for {
              _ <- pgUpdate(
                "insert into test (text) values (?)",
                _.setString(1, "example1")
              )
              results <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "example1"),
                rs => rs.getString("text")
              )
            } yield assertTrue(results.contains("example1"))
          }
        },
        test("pgSelectOne not found") {
          pgWithTransaction {
            for {
              results <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "notfound"),
                rs => rs.getString("text")
              )
            } yield assertTrue(results.isEmpty)
          }
        },
        test("pgSelectOne multiple results") {
          pgWithTransaction {
            for {
              _ <- pgUpdate(
                "insert into test (text) values (?)",
                _.setString(1, "example1")
              )
              _ <- pgUpdate(
                "insert into test (text) values (?)",
                _.setString(1, "example1")
              )
              results <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "example1"),
                rs => rs.getString("text")
              ).either
            } yield assertTrue(
              results.isLeft
                && results.left
                  .getOrElse("")
                  .toString == """Expected exactly one row in result, but got 2 rows. SQL: "select * from test where text = ?""""
            )
          }
        },
        test("pgInsertOne") {
          pgWithTransaction {
            for {
              _ <- pgInsertOne(
                "insert into test (text) values (?)",
                _.setString(1, "example_insert")
              )
              result <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "example_insert"),
                rs => rs.getString("text")
              )
            } yield assertTrue(result.contains("example_insert"))
          }
        },
        test("pgInsertWithId") {
          pgWithTransaction {
            for {
              id <- pgInsertWithId(
                "insert into test (text) values (?)",
                _.setString(1, "example_insert_with_id")
              )
              result <- pgSelectOne(
                "select * from test where id = ?",
                _.setLong(1, id.as[Long]),
                rs => rs.getString("text")
              )
            } yield assertTrue(result.contains("example_insert_with_id"))
          }
        },
        test("pgDeleteMany") {
          pgWithTransaction {
            for {
              _ <- pgInsertOne(
                "insert into test (text) values (?)",
                _.setString(1, "example_delete")
              )
              found <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "example_delete"),
                rs => rs.getString("text")
              )
              _ <- ZIO.log(s"Found before delete: $found")
              deleted <- pgDeleteMany(
                "delete from test where text = ?",
                _.setString(1, "example_delete")
              )
              foundAfter <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "example_delete"),
                rs => rs.getString("text")
              )
              _ <- ZIO.log(s"Found after delete: $foundAfter")
            } yield assertTrue(
              found.contains(
                "example_delete"
              ) && deleted == 1 && foundAfter.isEmpty
            )
          }
        },
        test("pgDeleteOne success") {
          pgWithTransaction {
            for {
              _ <- pgInsertOne(
                "insert into test (text) values (?)",
                _.setString(1, "example_delete_one")
              )
              found <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "example_delete_one"),
                rs => rs.getString("text")
              )
              _ <- ZIO.log(s"Found before delete: $found")
              deleted <- pgDeleteOne(
                "delete from test where text = ?",
                _.setString(1, "example_delete_one")
              )
              foundAfter <- pgSelectOne(
                "select * from test where text = ?",
                _.setString(1, "example_delete_one"),
                rs => rs.getString("text")
              )
              _ <- ZIO.log(s"Found after delete: $foundAfter")
            } yield assertTrue(
              found.contains(
                "example_delete_one"
              ) && deleted && foundAfter.isEmpty
            )
          }
        },
        test("pgDeleteOne not found") {
          pgWithTransaction {
            for {
              deleted <- pgDeleteOne(
                "delete from test where text = ?",
                _.setString(1, "not_found")
              )
            } yield assertTrue(!deleted)
          }
        },
        test("pgDeleteOne multiple results") {
          pgWithTransaction {
            for {
              _ <- pgInsertOne(
                "insert into test (text) values (?)",
                _.setString(1, "example_delete_many")
              )
              _ <- pgInsertOne(
                "insert into test (text) values (?)",
                _.setString(1, "example_delete_many")
              )
              deleted <- pgDeleteOne(
                "delete from test where text = ?",
                _.setString(1, "example_delete_many")
              ).either
            } yield assertTrue(
              deleted.isLeft
                && deleted.left
                  .getOrElse("")
                  .toString == """Expected exactly one row to be affected, but got 2 rows. SQL: "delete from test where text = ?""""
            )
          }
        }
      ).provideLayer(transactionManagerLayer)
    ) @@ TestAspect.before((for {
      dotenv <- ZIO.attempt(DotenvBuilder().filename(".env_test").load())
      _ <- TestSystem.putEnv("PGSQL_PASSWORD", dotenv.get("PGSQL_PASSWORD"))
    } yield ()).ignore)

  private def props(config: PgSqlConfig): Properties = {
    val props = new Properties()
    props.setProperty("user", config.user)
    props.setProperty("password", config.password)
    props
  }

  private val dataConnectionLayer: ZLayer[Any, Throwable, Connection] =
    ZLayer.scoped {
      ZIO.acquireRelease(
        ZIO
          .config(PgSqlConfig.pgSqlConfig)
          .flatMap(config =>
            ZIO.attempt {
              val connection =
                DriverManager.getConnection(config.url, props(config))
              populateConnection(connection)
              connection
            }
          )
      )(connection => ZIO.attempt(connection.close()).orDie)
    }

  private val transactionManagerLayer
      : ZLayer[Any, InfraFailure, TransactionManagerPg] =
    ZLayer.fromZIO(
      ZIO.config(PgSqlConfig.pgSqlConfig).orDie
    ) >>> DataSourcePg.layer >>>
      ZLayer.fromFunction((dataSourcePg: DataSourcePg) => {
        populateDatasource(dataSourcePg)
        TransactionManagerPg(dataSourcePg)
      })

  private def populateDatasource(dataSourcePg: DataSourcePg): Unit =
    Using.resource(dataSourcePg.dataSource.getConnection) { connection =>
      populateConnection(connection)
    }

  private def populateConnection(connection: Connection): Unit = {
    try {
      val statement = connection.createStatement()
      statement.execute("CREATE SCHEMA IF NOT EXISTS vocabla_test")
      statement.execute("SET search_path TO vocabla_test")
      statement.execute(
        "CREATE TEMP TABLE test ( id SERIAL PRIMARY KEY, text TEXT );"
      )
      statement.close()
    } catch {
      case e: Exception =>
        throw new RuntimeException("Failed to populate database", e)
    }
  }
}
