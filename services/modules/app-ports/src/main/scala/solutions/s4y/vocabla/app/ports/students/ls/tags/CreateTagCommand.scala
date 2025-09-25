package solutions.s4y.vocabla.app.ports.students.ls.tags

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Command to create a new tag.")
final case class CreateTagCommand(
    @description(
      "Tag to be created."
    )
    tag: Tag
)

object CreateTagCommand:
  given (using IdentifierSchema): Schema[CreateTagCommand] = Schema.derived
