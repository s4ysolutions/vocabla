package solutions.s4y.vocabla.app.ports

import solutions.s4y.vocabla.app.ports.errors.{InfraFailure, NotAuthorized}
import solutions.s4y.vocabla.domain.identity.Identifier.given
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Tag, UserContext}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema, derived}
import zio.ZIO

@description("Command to create a new tag.")
final case class CreateTagCommand(
    @description(
      "Tag to be created."
    )
    tag: Tag
)

object CreateTagCommand:
  @description("Response containing the ID of the newly created tag.")
  final case class Response(
      @description("ID of the newly created tag.")
      tagId: Identifier[Tag]
  )
  given (using IdentifierSchema): Schema[CreateTagCommand] = Schema.derived

  given (using IdentifierSchema): Schema[Response] = Schema.derived

@description("Use case for creating a new tag.")
trait CreateTagUseCase:
  def apply[R](
      command: CreateTagCommand
  ): ZIO[
    R & UserContext,
    InfraFailure | NotAuthorized,
    CreateTagCommand.Response
  ]
