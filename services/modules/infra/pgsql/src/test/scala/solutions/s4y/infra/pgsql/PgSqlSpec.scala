package solutions.s4y.infra.pgsql

import io.github.cdimascio.dotenv.{Dotenv, DotenvBuilder}
import solutions.s4y.infra.pgsql.tx.TransactionManagerPg
import solutions.s4y.vocabla.app.repo.tx.{
  TransactionContext,
  TransactionManager
}
import zio.test.Assertion.equalTo
import zio.test.{TestAspect, TestSystem, ZIOSpecDefault, assert, assertTrue}
import zio.{ZIO, ZLayer}

import java.sql.{Connection, DriverManager, Statement}
import java.util.Properties
import scala.util.Using

object PgSqlSpec extends ZIOSpecDefault {

  override def spec = suite("org.postgresql\" % \"postgresql")(
    suite("Connection") {
      test("connect to test database") {
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
        } yield assert(rs1)(equalTo(Seq("example", "example2"))) &&
          assert(rs2)(equalTo(Seq("example")))
      }
      // test("") {}
    }.provideLayer(dataConnectionLayer),
    suite("TransactionContext")(
      test("TransactionManager is available") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          _ <- ZIO.attempt(transactionManager.transaction(ZIO.unit))
        } yield assertTrue(
          transactionManager.isInstanceOf[TransactionManagerPg]
        )
      },
      test("TransactionManager provides transaction context") {
        for {
          transactionManager <- ZIO.service[TransactionManager]
          tx <- transactionManager.transaction {
            ZIO.service[TransactionContext]
          }
        } yield assertTrue(tx.isInstanceOf[TransactionContext])
      }
    ).provideLayer(transactionManagerLayer)
  ) @@ TestAspect.before(for {
    dotenv <- ZIO.attempt(DotenvBuilder().filename(".env_test").load())
    _ <- TestSystem.putEnv("PGSQL_PASSWORD", dotenv.get("PGSQL_PASSWORD"))
  } yield ())

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
      : ZLayer[Any, String, TransactionManagerPg] =
    ZLayer.fromZIO(
      ZIO.config(PgSqlConfig.pgSqlConfig).orDie
    ) >>> DataSourcePg.live >>>
      ZLayer.fromFunction((dataSourcePg: DataSourcePg) => {
        populateDatasource(dataSourcePg)
        TransactionManagerPg(dataSourcePg)
      })

  private def populateDatasource(dataSourcePg: DataSourcePg) = {
    val connection = dataSourcePg.dataSource.getConnection
    populateConnection(connection)
  }

  private def populateConnection(connection: Connection) = {
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
