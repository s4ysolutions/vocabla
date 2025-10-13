package solutions.s4y.vocabla.app.ports.students.entries.entry_get

import solutions.s4y.vocabla.domain.{Entry, User}
import solutions.s4y.vocabla.domain.User.{Admin, Student}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to get an entry by ID.")
final case class GetEntryCommand(
    @description("ID of the user making the request.")
    userId: Identifier[User],
    @description("ID of the entry to retrieve.")
    entryId: Identifier[Entry]
)

object GetEntryCommand:
  given (using IdentifierSchema): Schema[GetEntryCommand] =
    DeriveSchema.gen[GetEntryCommand]
