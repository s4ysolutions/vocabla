package solutions.s4y.vocabla.app.ports

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.Identifier.given
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.IO
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to get an entry by ID.")
final case class GetEntryCommand(
    @description("ID of the entry to retrieve.")
    entryId: Identifier[Entry]
)

object GetEntryCommand:
  @description("Response containing the entry if found.")
  final case class Response(
      @description("The retrieved entry.")
      entry: Option[Entry]
  )
  given (using IdentifierSchema): Schema[GetEntryCommand] =
    DeriveSchema.gen[GetEntryCommand]

  given (using IdentifierSchema): Schema[Response] =
    DeriveSchema.gen[Response]

@description("Use case for retrieving an entry by ID.")
trait GetEntryUseCase:
  def apply(
      command: GetEntryCommand
  ): IO[ServiceFailure, GetEntryCommand.Response]
