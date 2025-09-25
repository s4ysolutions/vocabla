package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.pgUpdateOne
import solutions.s4y.vocabla.app.repo.KnownLanguagesRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{Lang, User}
import zio.{ULayer, ZIO, ZLayer}

class KnownLanguagesRepositoryPg
    extends KnownLanguagesRepository[TransactionContextPg] {
  def addKnownLanguage[R](
      studentId: Identifier[User.Student],
      language: Lang.Code
  )(using TransactionContextPg): ZIO[R, InfraFailure, Unit] =
    pgUpdateOne(
      """UPDATE users
        |SET learning_settings = jsonb_set(
        |  COALESCE(learning_settings, '{"learnLanguages":[],"knownLanguages":[]}'),
        |  '{knownLanguages}',
        |  (
        |   CASE
        |      -- Handle empty array case
        |      WHEN learning_settings->'knownLanguages' = '[]'::jsonb
        |        OR learning_settings->'knownLanguages' IS NULL
        |      THEN to_jsonb(ARRAY[?])  -- Create new array with just the new language
        |
        |      -- Handle existing array case
        |      WHEN EXISTS (
        |        SELECT 1
        |        FROM jsonb_array_elements_text(learning_settings->'knownLanguages') elem
        |        WHERE LOWER(elem) = LOWER(?)
        |      )
        |      THEN learning_settings->'knownLanguages'  -- No change, already exists
        |
        |      -- Add to existing array
        |      ELSE (
        |        SELECT jsonb_agg(elem) || to_jsonb(?)::jsonb
        |        FROM jsonb_array_elements_text(learning_settings->'knownLanguages') elem
        |      )
        |    END
        |  ),
        |  true
        |)
        |WHERE id = ? AND student IS NOT NULL""".stripMargin,
      st => {
        st.setString(1, s"$language")
        st.setString(2, s"$language")
        st.setString(3, s"$language")
        st.setLong(4, studentId.as[Long])
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
               COALESCE(
               (
                 SELECT jsonb_agg(elem)
                 FROM jsonb_array_elements_text(learning_settings->'knownLanguages') elem
                 WHERE LOWER(elem) != LOWER(?)
                 ),
       '[]'::jsonb
               ),
               true
             )
             WHERE id = ? AND student IS NOT NULL """,
      st => {
        st.setString(1, language)
        st.setLong(2, studentId.as[Long])
      }
    ).unit
}

object KnownLanguagesRepositoryPg:
  def layer: ULayer[KnownLanguagesRepositoryPg] =
    ZLayer.succeed(new KnownLanguagesRepositoryPg)
