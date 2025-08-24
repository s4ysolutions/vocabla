package solutions.s4y.vocabla.app.ports

import solutions.s4y.vocabla.app.ports
import solutions.s4y.vocabla.domain.identity.Identifier.given
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, Student, Tag}
import zio.schema.annotation.{caseName, description, fieldName, recordName}
import zio.schema.validation.Validation
import zio.schema.{DeriveSchema, Schema}
import zio.{Chunk, ZIO}

@description("Command to create a new vocabulary entry.")
final case class CreateEntryCommand(
    @description(
      "Entry to be added to the vocabulary."
    )
    entry: Entry,
    @description("IDs of tags to be associated with the entry.")
    tagIds: Chunk[Identifier[Tag]],
    @description("ID of the student who owns the entry.")
    ownerId: Identifier[Student]
)

object CreateEntryCommand:
  @description("Response containing the ID of the newly created entry.")
  final case class Response(
      @description("ID of the newly created entry.")
      entryId: Identifier[Entry]
  )
  given (using IdentifierSchema): Schema[CreateEntryCommand] =
    DeriveSchema.gen[CreateEntryCommand]

  given (using IdentifierSchema): Schema[Response] =
    DeriveSchema.gen[Response]

@description("Use case for creating a new vocabulary entry.")
trait CreateEntryUseCase:
  def apply[R](
      command: CreateEntryCommand
  ): ZIO[R, String, CreateEntryCommand.Response]
