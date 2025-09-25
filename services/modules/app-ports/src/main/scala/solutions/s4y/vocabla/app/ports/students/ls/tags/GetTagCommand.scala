package solutions.s4y.vocabla.app.ports.students.ls.tags

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.User.Student
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to get a tag by ID.")
final case class GetTagCommand(
    @description("ID of the owner (student) of the tag.")
    ownerId: Identifier[Student],
    @description("ID of the tag to retrieve.")
    tagId: Identifier[Tag]
)

object GetTagCommand:
  given (using IdentifierSchema): Schema[GetTagCommand] =
    DeriveSchema.gen[GetTagCommand]
