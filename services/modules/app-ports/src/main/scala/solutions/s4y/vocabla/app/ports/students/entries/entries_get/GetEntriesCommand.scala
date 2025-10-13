package solutions.s4y.vocabla.app.ports.students.entries.entries_get

import solutions.s4y.vocabla.domain.User.{Admin, Student}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Lang, Tag, User}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Request to get entries with optional filters.")
final case class GetEntriesCommand(
                                    @description("ID of the user making the request.")
    userId: Identifier[User],
                                    @description("ID of the owner to filter entries by.")
    ownerId: Option[Identifier[User]],
                                    @description("Tag IDs to filter entries by.")
    tagIds: Chunk[Identifier[Tag]],
                                    @description("Languages to filter entries by.")
    langs: Chunk[Lang.Code],
                                    @description("Text to search for in entries.")
    text: Option[String]
)

object GetEntriesCommand:
  given (using IdentifierSchema): Schema[GetEntriesCommand] =
    DeriveSchema.gen[GetEntriesCommand]
