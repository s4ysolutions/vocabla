package solutions.s4y.vocabla.app.ports.students.settings.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.given
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Tag, UserContext}
import zio.ZIO
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Use case for creating a new tag.")
trait CreateTagUseCase:
  def apply(
      command: CreateTagCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    CreateTagResponse
  ]
