package solutions.s4y.vocabla.app.ports.tag_get

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to get a tag by ID.")
final case class GetTagRequest(
    @description("ID of the tag to retrieve.")
    tagId: Identifier[Tag]
)

object GetTagRequest:
  given (using IdentifierSchema): Schema[GetTagRequest] =
    DeriveSchema.gen[GetTagRequest]
