package solutions.s4y.vocabla.app.ports.students.entries.entries_get

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for retrieving entries with optional filters.")
trait GetEntriesUseCase:
  def apply(
      command: GetEntriesCommand
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, GetEntriesResponse]
