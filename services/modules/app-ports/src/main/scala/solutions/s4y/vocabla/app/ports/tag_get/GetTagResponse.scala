package solutions.s4y.vocabla.app.ports.tag_get

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("CreateTagResponse containing the tag if found.")
final case class GetTagResponse(
    @description("The retrieved tag.")
    tag: Option[Tag]
)

object GetTagResponse:
  given (using IdentifierSchema): Schema[GetTagResponse] =
    DeriveSchema.gen[GetTagResponse]
