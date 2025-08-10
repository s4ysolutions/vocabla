package solutions.s4y.infra.pgsql

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{TestAspect, ZIOSpecDefault, assertCompletes, assert}

import java.sql.{DriverManager, ResultSet, Statement}
import java.util.Properties

object PgSqlSpec extends ZIOSpecDefault {
  override def spec = suite("org.postgresql\" % \"postgresql") {
    test("connect to test database") {
      for {
        config <- ZIO.config(PgSqlConfig.pgSqlConfig)
        conn <- ZIO.attempt(
          DriverManager.getConnection(config.url, props(config))
        )
        rs1 <- ZIO.attempt {
          val st = conn.createStatement()
          val rs = st.executeQuery("select * from vocabla.test")
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
            conn.prepareStatement("select * from vocabla.test where word = ?");
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
  } @@ TestAspect.withLiveEnvironment

  private def props(config: PgSqlConfig): Properties = {
    val props = new Properties()
    props.setProperty("user", config.user)
    props.setProperty("password", config.password)
    props
  }
}
