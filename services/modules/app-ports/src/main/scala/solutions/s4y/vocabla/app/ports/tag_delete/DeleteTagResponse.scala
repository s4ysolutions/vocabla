package solutions.s4y.vocabla.app.ports.tag_delete

import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description(
  "DeleteTagResponse containing the result of the deletion operation."
)
final case class DeleteTagResponse(
    @description("Actual deletion status.")
    deleted: Boolean
)

object DeleteTagResponse:
  given (using IdentifierSchema): Schema[DeleteTagResponse] =
    DeriveSchema.gen[DeleteTagResponse]
