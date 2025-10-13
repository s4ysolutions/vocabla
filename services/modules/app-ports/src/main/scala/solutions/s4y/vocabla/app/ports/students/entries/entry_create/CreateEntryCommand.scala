package solutions.s4y.vocabla.app.ports.students.entries.entry_create

import solutions.s4y.vocabla.domain.User.{Admin, Student}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, Tag, User}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to create a new vocabulary entry.")
final case class CreateEntryCommand(
    @description("ID of the user making the request.")
    userId: Identifier[User],
    @description(
      "Entry to be added to the vocabulary."
    )
    entry: Entry,
    @description("IDs of tags to be associated with the entry.")
    tagIds: Chunk[Identifier[Tag]]
)

object CreateEntryCommand:
  given (using IdentifierSchema): Schema[CreateEntryCommand] =
    DeriveSchema.gen[CreateEntryCommand]
