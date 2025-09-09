package solutions.s4y.vocabla.app.ports.entry_get

import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to get an entry by ID.")
final case class GetEntryRequest(
    @description("ID of the entry to retrieve.")
    entryId: Identifier[Entry]
)

object GetEntryRequest:
  given (using IdentifierSchema): Schema[GetEntryRequest] =
    DeriveSchema.gen[GetEntryRequest]
