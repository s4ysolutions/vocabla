package solutions.s4y.vocabla.app.ports.students.entries.entry_delete

import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description(
  "Response indicating whether the entry was successfully deleted."
)
final case class DeleteEntryResponse(
    @description("True if the entry was deleted, false if it didn't exist.")
    deleted: Boolean
)

object DeleteEntryResponse:
  given Schema[DeleteEntryResponse] = DeriveSchema.gen[DeleteEntryResponse]

