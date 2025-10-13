package solutions.s4y.vocabla.app.ports.students.entries.entry_get

import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("CreateTagResponse containing the entry if found.")
final case class GetEntryResponse(
                                   @description("The retrieved entry.")
                                   entry: Option[Entry]
                                 )

object GetEntryResponse:
  given (using IdentifierSchema): Schema[GetEntryResponse] =
    DeriveSchema.gen[GetEntryResponse]
