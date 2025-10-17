package solutions.s4y.vocabla.app.ports.students.entries.entry_update

import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description(
  "Response indicating whether the entry was successfully updated."
)
final case class UpdateEntryResponse(
    @description("True if the entry was updated, false if it didn't exist.")
    updated: Boolean
)

object UpdateEntryResponse:
  given Schema[UpdateEntryResponse] = DeriveSchema.gen[UpdateEntryResponse]

