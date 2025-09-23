package solutions.s4y.vocabla.infra.pgsql

import org.slf4j.LoggerFactory
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.{pgSelectOne, pgUpdateOne, pgSelectMany, pgInsertOne, pgDeleteOne}
import solutions.s4y.vocabla.app.repo.UserRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.{Lang, Tag, User}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import zio.json.{DecoderOps, EncoderOps, JsonCodec}
import zio.{Chunk, ZIO, ZLayer}

import scala.util.Using

class UserRepositoryPg(using IdentifierSchema)
    extends UserRepository[TransactionContextPg]:
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
  ): ZIO[R, InfraFailure, UserRepository.LearningSettings] =
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
              case Left(error) =>
                // Log error and return empty settings as fallback
                LanguageSettings(Chunk.empty, Chunk.empty)
            }
          }
        }
      ).zipPar(
        pgSelectMany(
          """SELECT t.id
             FROM user_learning_tags ult
             JOIN tags t ON ult.tag_id = t.id
             WHERE ult.user_id = ?""",
          st => st.setLong(1, studentId.as[Long]),
          rs => rs.getLong(1).identifier[Tag]
        )
      )
    } yield UserRepository.LearningSettings(
      learnLanguages = languageSettings.map(_.learnLanguages).getOrElse(Chunk.empty),
      knownLanguages = languageSettings.map(_.knownLanguages).getOrElse(Chunk.empty),
      tags = tags
    )

  // Fine-grained language management methods only
  def addLearnLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TransactionContextPg): ZIO[R, InfraFailure, Unit] =
    pgUpdateOne(
      """UPDATE users
         SET learning_settings = jsonb_set(
           COALESCE(learning_settings, '{"learnLanguages":[],"knownLanguages":[]}'),
           '{learnLanguages}',
           COALESCE(learning_settings->'learnLanguages', '[]') || ?::jsonb,
           true
         )
         WHERE id = ? AND student IS NOT NULL""",
      st => {
        st.setString(1, s""""$language"""")
        st.setLong(2, studentId.as[Long])
      }
    ).unit

  def removeLearnLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TransactionContextPg): ZIO[R, InfraFailure, Unit] =
    pgUpdateOne(
      """UPDATE users
         SET learning_settings = jsonb_set(
           learning_settings,
           '{learnLanguages}',
           (
             SELECT jsonb_agg(elem)
             FROM jsonb_array_elements_text(learning_settings->'learnLanguages') elem
             WHERE elem != ?
           ),
           true
         )
         WHERE id = ? AND student IS NOT NULL AND learning_settings IS NOT NULL""",
      st => {
        st.setString(1, language)
        st.setLong(2, studentId.as[Long])
      }
    ).unit

  def addKnownLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TransactionContextPg): ZIO[R, InfraFailure, Unit] =
    pgUpdateOne(
      """UPDATE users
         SET learning_settings = jsonb_set(
           COALESCE(learning_settings, '{"learnLanguages":[],"knownLanguages":[]}'),
           '{knownLanguages}',
           COALESCE(learning_settings->'knownLanguages', '[]') || ?::jsonb,
           true
         )
         WHERE id = ? AND student IS NOT NULL""",
      st => {
        st.setString(1, s""""$language"""")
        st.setLong(2, studentId.as[Long])
      }
    ).unit

  def removeKnownLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TransactionContextPg): ZIO[R, InfraFailure, Unit] =
    pgUpdateOne(
      """UPDATE users
         SET learning_settings = jsonb_set(
           learning_settings,
           '{knownLanguages}',
           (
             SELECT jsonb_agg(elem)
             FROM jsonb_array_elements_text(learning_settings->'knownLanguages') elem
             WHERE elem != ?
           ),
           true
         )
         WHERE id = ? AND student IS NOT NULL AND learning_settings IS NOT NULL""",
      st => {
        st.setString(1, language)
        st.setLong(2, studentId.as[Long])
      }
    ).unit

  // Bulk update method for backward compatibility (languages only, tags handled by existing association methods)
  def updateLearningLanguages[R](
      studentId: Identifier[User.Student],
      learnLanguages: Chunk[Lang.Code],
      knownLanguages: Chunk[Lang.Code]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Unit] =
    val languageSettings = LanguageSettings(learnLanguages, knownLanguages)
    pgUpdateOne(
      "UPDATE users SET learning_settings = ?::jsonb WHERE id = ? AND student IS NOT NULL",
      st => {
        st.setString(1, languageSettings.toJson)
        st.setLong(2, studentId.as[Long])
      }
    ).unit

// Helper case class for language settings only (no tags)
private case class LanguageSettings(
  learnLanguages: Chunk[Lang.Code],
  knownLanguages: Chunk[Lang.Code]
)

private object LanguageSettings:
  given JsonCodec[LanguageSettings] = zio.json.DeriveJsonCodec.gen[LanguageSettings]

object UserRepositoryPg:
  given IdentifierSchema with
    type ID = Long
    val schema: zio.schema.Schema[Long] = zio.schema.Schema.primitive[Long]
  end given

  private val init = Seq(
    "DROP TABLE IF EXISTS user_learning_tags CASCADE",
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
     learning_settings JSONB,
     CONSTRAINT has_at_least_one_role CHECK (
        student IS NOT NULL OR
        admin IS NOT NULL
    ),
     CONSTRAINT learning_settings_only_for_students CHECK (
        (student IS NULL AND learning_settings IS NULL) OR
        (student IS NOT NULL)
    )
    )""",
    """CREATE TABLE user_learning_tags (
      user_id BIGINT NOT NULL,
      tag_id BIGINT NOT NULL,
      PRIMARY KEY (user_id, tag_id),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
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
