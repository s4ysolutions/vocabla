package solutions.s4y.vocabla.app.ports.tag_create

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.{Schema, derived}
import zio.schema.annotation.description

@description("CreateTagResponse containing the ID of the newly created tag.")
final case class CreateTagResponse(
    @description("ID of the newly created tag.")
    tagId: Identifier[Tag]
)

object CreateTagResponse:
  given (using IdentifierSchema): Schema[CreateTagResponse] = Schema.derived
