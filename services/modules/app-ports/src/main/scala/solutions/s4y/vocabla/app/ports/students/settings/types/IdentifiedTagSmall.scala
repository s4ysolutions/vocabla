package solutions.s4y.vocabla.app.ports.students.settings.types

import solutions.s4y.vocabla.domain.TagSmall
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}
import zio.schema.annotation.description

@description("A small tag with its identifier.")
final case class IdentifiedTagSmall(id: Identifier[TagSmall], e: TagSmall)

object IdentifiedTagSmall:
  given (using IdentifierSchema): Schema[IdentifiedTagSmall] =
    DeriveSchema.gen[IdentifiedTagSmall]

  given (using IdentifierSchema): JsonCodec[IdentifiedTagSmall] = {
    DeriveJsonCodec.gen[IdentifiedTagSmall]
  }
