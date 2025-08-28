package solutions.s4y.vocabla.app.ports

import solutions.s4y.vocabla.app.ports.errors.InfraFailure
import solutions.s4y.vocabla.domain.User
import solutions.s4y.vocabla.domain.identity.Identifier.given
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.{IO, ZIO}
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema, derived}

@description("Command to get an user by ID.")
final case class GetUserCommand(
    @description("ID of the user to retrieve.")
    userId: Identifier[User]
)

object GetUserCommand:
  @description("Response containing the entry if found.")
  final case class Response(
      @description("The retrieved entry.")
      user: Option[User]
  )
  given (using IdentifierSchema): Schema[GetUserCommand] = Schema.derived

  given (using IdentifierSchema): Schema[Response] = Schema.derived

@description("Use case for retrieving an user by ID.")
trait GetUserUseCase:
  def apply(
      command: GetUserCommand
  ): IO[InfraFailure, GetUserCommand.Response]
  def apply(
      id: Identifier[User]
  ): IO[String, Option[User]]
