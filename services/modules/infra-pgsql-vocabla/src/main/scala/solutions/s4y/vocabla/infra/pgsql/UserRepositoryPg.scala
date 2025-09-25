package solutions.s4y.vocabla.infra.pgsql

import org.slf4j.LoggerFactory
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.{pgSelectMany, pgSelectOne}
import solutions.s4y.vocabla.app.repo.UserRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Lang, LearningSettings, Tag, User}
import zio.json.{DecoderOps, JsonCodec}
import zio.{Chunk, ZIO, ZLayer}

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

  override def getLearningSettings[R](
      studentId: Identifier[User.Student]
  )(using
      TransactionContextPg
  ): ZIO[R, InfraFailure, LearningSettings] =
    for {
      // Run both queries in parallel using zipPar
      (languageSettings, tags) <- pgSelectOne(
        "SELECT learning_settings FROM users WHERE id = ? AND student IS NOT NULL",
        st => st.setLong(1, studentId.as[Long]),
        rs => {
          val jsonStr = rs.getString(1)
          if (jsonStr == null) {
            LanguageSettings(Chunk.empty, Chunk.empty)
          } else {
            jsonStr.fromJson[LanguageSettings] match {
              case Right(settings) => settings
              case Left(error)     =>
                // Log error and return empty settings as fallback
                LanguageSettings(Chunk.empty, Chunk.empty)
            }
          }
        }
      ).zipPar(
        pgSelectMany(
          """SELECT id
             FROM tags tags
             WHERE tags.ownerId = ?""",
          st => st.setLong(1, studentId.as[Long]),
          rs => rs.getLong(1).identifier[Tag]
        )
      )
    } yield LearningSettings(
      learnLanguages =
        languageSettings.map(_.learnLanguages).getOrElse(Chunk.empty),
      knownLanguages =
        languageSettings.map(_.knownLanguages).getOrElse(Chunk.empty),
      tags = tags
    )

private case class LanguageSettings(
    learnLanguages: Chunk[Lang.Code],
    knownLanguages: Chunk[Lang.Code]
)

private object LanguageSettings:
  given JsonCodec[LanguageSettings] =
    zio.json.DeriveJsonCodec.gen[LanguageSettings]

object UserRepositoryPg:
  private val init = Seq(
    """DO $$
    BEGIN
        CREATE TYPE user_admin AS ( active BOOLEAN );
      EXCEPTION
        WHEN duplicate_object THEN null;
    END
    $$""",
    """DO $$
    BEGIN
        CREATE TYPE user_student AS ( nickname TEXT );
      EXCEPTION
        WHEN duplicate_object THEN null;
    END
    $$""",
    s"""CREATE TABLE IF NOT EXISTS  users (
     id SERIAL PRIMARY KEY,
     student user_student,
     admin user_admin,
     learning_settings JSONB,
     CONSTRAINT has_at_least_one_role CHECK (
        student IS NOT NULL OR
        admin IS NOT NULL
    ),
     CONSTRAINT learning_settings_only_for_students CHECK (
        (student IS NULL AND learning_settings IS NULL) OR
        (student IS NOT NULL)
    )
    )"""
  )

  def layer: ZLayer[DataSourcePg, InfraFailure, UserRepositoryPg] =
    ZLayer {
      ZIO
        .serviceWithZIO[DataSourcePg] { ds =>
          ZIO
            .fromTry {
              Using.Manager { use =>
                val connection = use(ds.dataSource.getConnection)
                val statement = use(connection.createStatement())

                try {
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
                } catch {
                  case th: Throwable =>
                    log.error(
                      "Error during UserRepositoryPg initialization",
                      th
                    )
                    throw th
                }
              }
            }
            .orDie *> ZIO.logDebug("UserRepositoryPg initialized")
        }
        .as(new UserRepositoryPg)
    }
  private val log = LoggerFactory.getLogger(UserRepositoryPg.getClass)
end UserRepositoryPg
