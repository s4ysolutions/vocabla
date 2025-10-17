package solutions.s4y.vocabla.app.ports.students.entries.entry_update

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for updating a vocabulary entry.")
trait UpdateEntryUseCase:
  def apply(
      command: UpdateEntryCommand
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, UpdateEntryResponse]

