package solutions.s4y.vocabla.app.ports

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.identity.Identifier.given
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.Tag
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}
import zio.{IO, ZIO}

@description("Command to get a tag by ID.")
final case class GetTagCommand(
    @description("ID of the tag to retrieve.")
    tagId: Identifier[Tag]
)

object GetTagCommand:
  @description("Response containing the tag if found.")
  final case class Response(
      @description("The retrieved tag.")
      tag: Option[Tag]
  )
  given (using IdentifierSchema): Schema[GetTagCommand] =
    DeriveSchema.gen[GetTagCommand]

  given (using IdentifierSchema): Schema[Response] =
    DeriveSchema.gen[Response]

@description("Use case for retrieving a tag by ID.")
trait GetTagUseCase:
  def apply(
      command: GetTagCommand
  ): IO[ServiceFailure, GetTagCommand.Response]
