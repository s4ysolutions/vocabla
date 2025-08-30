package solutions.s4y.vocabla.endpoint.http.rest.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.{CreateTagCommand, CreateTagUseCase}
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.middleware.AuthenticationError
import solutions.s4y.vocabla.endpoint.http.rest.prefix
import zio.ZIO
import zio.http.*
import zio.http.Method.POST
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint, orOutError}

import java.util.Locale

object CreateTag:

  given Locale = Locale.ENGLISH

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateTagCommand,
    ServiceFailure | NotAuthorized | AuthenticationError,
    CreateTagCommand.Response,
    AuthType.Bearer.type
  ] = Endpoint(POST / prefix / "tags")
    .tag("Tags")
    .in[CreateTagCommand]
    .out[CreateTagCommand.Response]
    .outError[ServiceFailure](Status.InternalServerError)
    .orOutError[NotAuthorized](Status.Forbidden)
    .orOutError[AuthenticationError](Status.Unauthorized)
    .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[CreateTagUseCase & UserContext, Response] =
    endpoint.implement(request =>
      ZIO.serviceWithZIO[CreateTagUseCase](_(CreateTagCommand(request.tag)))
    )
