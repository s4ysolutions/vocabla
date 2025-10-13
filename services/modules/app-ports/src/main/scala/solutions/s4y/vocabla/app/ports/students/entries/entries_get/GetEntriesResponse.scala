package solutions.s4y.vocabla.app.ports.students.entries.entries_get

import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.{Identified, IdentifierSchema}
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Response containing a list of entries.")
final case class GetEntriesResponse(
    @description("List of retrieved entries.")
    entries: Chunk[Identified[Entry]]
)

object GetEntriesResponse:
  given (using IdentifierSchema): Schema[GetEntriesResponse] =
    DeriveSchema.gen[GetEntriesResponse]
