package solutions.s4y.vocabla.app.ports.tag_create

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.{Schema, derived}
import zio.schema.annotation.description

@description("Command to create a new tag.")
final case class CreateTagCommand(
    @description(
      "Tag to be created."
    )
    tag: Tag
)

object CreateTagCommand:
  given (using IdentifierSchema): Schema[CreateTagCommand] = Schema.derived
