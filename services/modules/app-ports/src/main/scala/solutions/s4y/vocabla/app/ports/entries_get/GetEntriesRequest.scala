package solutions.s4y.vocabla.app.ports.entries_get

import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Lang, Tag, User}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Request to get entries with optional filters.")
final case class GetEntriesRequest(
    @description("ID of the owner to filter entries by.")
    ownerId: Option[Identifier[User]],
    @description("Tag IDs to filter entries by.")
    tagId: Chunk[Identifier[Tag]],
    @description("Languages to filter entries by.")
    lang: Chunk[Lang.Code],
    @description("Text to search for in entries.")
    text: Option[String]
)

object GetEntriesRequest:
  given (using IdentifierSchema): Schema[GetEntriesRequest] =
    DeriveSchema.gen[GetEntriesRequest]
