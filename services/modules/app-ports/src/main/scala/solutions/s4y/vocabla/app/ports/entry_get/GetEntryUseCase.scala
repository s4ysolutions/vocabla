package solutions.s4y.vocabla.app.ports.entry_get

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for retrieving an entry by ID.")
trait GetEntryUseCase:
  def apply(
      command: GetEntryRequest
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, GetEntryResponse]
