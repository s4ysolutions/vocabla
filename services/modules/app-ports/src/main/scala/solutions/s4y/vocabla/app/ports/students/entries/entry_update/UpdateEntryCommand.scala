package solutions.s4y.vocabla.app.ports.students.entries.entry_update

import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, Tag, User}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to update an existing vocabulary entry.")
final case class UpdateEntryCommand(
    @description("ID of the user making the request.")
    userId: Identifier[User],
    entryId: Identifier[Entry],
    headword: Option[Entry.Headword],
    definitions: Option[Chunk[Entry.Definition]],
    tagIds: Option[Chunk[Identifier[Tag]]]
)

object UpdateEntryCommand:
  given (using IdentifierSchema): Schema[UpdateEntryCommand] =
    DeriveSchema.gen[UpdateEntryCommand]
