package solutions.s4y.vocabla.app.ports.entry_create

import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description(
  "CreateTagResponse containing the ID of the newly created entry."
)
final case class CreateEntryResponse(
    @description("ID of the newly created entry.")
    entryId: Identifier[Entry]
)

object CreateEntryResponse:
  given (using IdentifierSchema): Schema[CreateEntryResponse] =
    DeriveSchema.gen[CreateEntryResponse]
