package solutions.s4y.vocabla.app.ports.students.settings.tags

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.User.Student
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Command to delete a tag by ID.")
final case class DeleteTagCommand(
    @description("ID of the owner (student) of the tag.")
    ownerId: Identifier[Student],
    @description("ID of the tag to delete.")
    tagId: Identifier[Tag]
)

object DeleteTagCommand:
  given (using IdentifierSchema): Schema[DeleteTagCommand] =
    DeriveSchema.gen[DeleteTagCommand]
