package solutions.s4y.vocabla.endpoint.http.rest.tags

import solutions.s4y.vocabla.app.ports.{CreateTagCommand, CreateTagUseCase}
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.auth.UserContext
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.{
  AuthenticationError,
  ErrorService
}
import solutions.s4y.vocabla.endpoint.http.rest.prefix
import zio.ZIO
import zio.http.Method.POST
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.*

object CreateTag:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateTagCommand,
    ErrorResponse,
    CreateTagCommand.Response,
    AuthType.Bearer.type
  ] = Endpoint(POST / prefix / "tags")
    .tag("Tags")
    .in[CreateTagCommand]
    .out[CreateTagCommand.Response]
    .outErrors[ErrorResponse](
      HttpCodec.error[ErrorService](Status.InternalServerError),
      HttpCodec.error[AuthenticationError](Status.Unauthorized)
    )
    .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[CreateTagUseCase & UserContext, Response] =
    endpoint.implement { request =>
      (for {
        uc <- ZIO.service[UserContext]
        _ <- ZIO.logDebug("CreateTag called by user " + uc.id)
        useCase <- ZIO.service[CreateTagUseCase]
        response <- useCase(CreateTagCommand(request.tag))
      } yield response).mapError(error => ErrorService(error))
    }
