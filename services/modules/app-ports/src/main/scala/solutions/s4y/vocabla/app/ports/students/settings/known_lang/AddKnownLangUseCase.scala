package solutions.s4y.vocabla.app.ports.students.settings.known_lang

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for adding a language the student already knows.")
trait AddKnownLangUseCase:
  def apply(
      command: AddKnownLangCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    AddKnownLangResponse
  ]
