package solutions.s4y.vocabla.app.ports.students.entries.entry_delete

import solutions.s4y.vocabla.domain.{Entry, User}
import solutions.s4y.vocabla.domain.User.{Admin, Student}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to delete a vocabulary entry.")
final case class DeleteEntryCommand(
    @description("ID of the user making the request.")
    userId: Identifier[User],
    @description("ID of the entry to be deleted.")
    entryId: Identifier[Entry]
)

object DeleteEntryCommand:
  given (using IdentifierSchema): Schema[DeleteEntryCommand] =
    DeriveSchema.gen[DeleteEntryCommand]
