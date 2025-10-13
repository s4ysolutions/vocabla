package solutions.s4y.vocabla.app.ports.students.entries.entry_delete

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for deleting a vocabulary entry.")
trait DeleteEntryUseCase:
  def apply(
      command: DeleteEntryCommand
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, DeleteEntryResponse]

