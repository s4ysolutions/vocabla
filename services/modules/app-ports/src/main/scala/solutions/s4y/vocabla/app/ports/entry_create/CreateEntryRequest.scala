package solutions.s4y.vocabla.app.ports.entry_create

import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, Tag, User}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to create a new vocabulary entry.")
final case class CreateEntryRequest(
    @description(
      "Entry to be added to the vocabulary."
    )
    entry: Entry,
    @description("IDs of tags to be associated with the entry.")
    tagIds: Chunk[Identifier[Tag]],
)

object CreateEntryRequest:
  given (using IdentifierSchema): Schema[CreateEntryRequest] =
    DeriveSchema.gen[CreateEntryRequest]
