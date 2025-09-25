package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.pgUpdateOne
import solutions.s4y.vocabla.app.repo.{LearnLanguagesRepository, UnknownLanguagesRepository}
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{Lang, User}
import zio.{ULayer, ZIO, ZLayer}

class KnownLanguagesRepositoryPg
    extends UnknownLanguagesRepository[TransactionContextPg] {
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
}

object KnownLanguagesRepositoryPg:
  def layer: ULayer[KnownLanguagesRepositoryPg] =
    ZLayer.succeed(new KnownLanguagesRepositoryPg)
