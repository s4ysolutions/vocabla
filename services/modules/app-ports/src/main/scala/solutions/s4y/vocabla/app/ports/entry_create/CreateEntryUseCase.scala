package solutions.s4y.vocabla.app.ports.entry_create

import solutions.s4y.vocabla.app.ports.entry_create.{
  CreateEntryRequest,
  CreateEntryResponse
}
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for creating a new vocabulary entry.")
trait CreateEntryUseCase:
  def apply(
      command: CreateEntryRequest
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, CreateEntryResponse]
