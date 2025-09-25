package solutions.s4y.vocabla.app.ports.students.settings.known_lang

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for removing a language the student wants to learn.")
trait RemoveKnownLangUseCase:
  def apply(
      command: RemoveKnownLangCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    RemoveKnownLangResponse
  ]
