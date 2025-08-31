package solutions.s4y.vocabla.infra.pgsql

import org.slf4j.LoggerFactory
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.pgSelectOne
import solutions.s4y.vocabla.app.repo.UserRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.User
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.{ZIO, ZLayer}

import scala.util.Using

class UserRepositoryPg extends UserRepository[TransactionContextPg]:
  override def get[R](
      userId: Identifier[User]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Option[User]] =
    pgSelectOne(
      "SELECT student, admin FROM users WHERE id = ?",
      st => st.setLong(1, userId.as[Long]),
      rs =>
        User(
          student = Option(rs.getObject(1)).map { obj =>
            val tuple = obj.asInstanceOf[org.postgresql.util.PGobject].getValue
            val nickname = tuple
              .substring(1, tuple.length - 1) // remove parentheses
              .split(",")(0) // get the first element
            User.Student(nickname)
          },
          admin = Option(rs.getObject(2)).map { obj =>
            val tuple = obj.asInstanceOf[org.postgresql.util.PGobject].getValue
            val active = tuple
              .substring(1, tuple.length - 1) // remove parentheses
              .toBoolean // convert to boolean
            User.Admin(active)
          }
        )
    )

object UserRepositoryPg:
  private val init = Seq(
    "DROP TABLE IF EXISTS users CASCADE",
    "DROP TYPE IF EXISTS user_admin",
    """CREATE TYPE user_admin AS (
      active BOOLEAN
    )""",
    "DROP TYPE IF EXISTS user_student",
    """CREATE TYPE user_student AS (
      nickname TEXT
    )""",
    """CREATE TABLE users (
     id SERIAL PRIMARY KEY,
     student user_student,
     admin user_admin,
     CONSTRAINT has_at_least_one_role CHECK (
        student IS NOT NULL OR
        admin IS NOT NULL
    )
    )"""
  )

  val layer: ZLayer[DataSourcePg, InfraFailure, UserRepositoryPg] =
    ZLayer {
      ZIO
        .serviceWithZIO[DataSourcePg] { ds =>
          ZIO.attempt {
            Using.Manager { use =>
              val connection = use(ds.dataSource.getConnection)
              val statement = use(connection.createStatement())

              init.foreach { sql =>
                log.info(s"Executing SQL: $sql")
                statement.execute(sql)
              }
              statement.execute("select count(*) from users")
              val rs = statement.getResultSet
              rs.next()
              val count = rs.getInt(1)
              if count == 0 then {
                log.info("Inserting default user")
                statement.execute(
                  "INSERT INTO users (student) VALUES (ROW('default_student'))"
                )
              }
            }
          }.orDie *> ZIO.logDebug("UserRepositoryPg initialized")
        }
        .as(new UserRepositoryPg)
    }
  private val log = LoggerFactory.getLogger(UserRepositoryPg.getClass)
end UserRepositoryPg
