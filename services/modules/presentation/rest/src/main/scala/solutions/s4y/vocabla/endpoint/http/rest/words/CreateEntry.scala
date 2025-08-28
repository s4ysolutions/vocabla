package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.app.ports.errors.{InfraFailure, NotAuthorized}
import solutions.s4y.vocabla.app.ports.{CreateEntryCommand, CreateEntryUseCase}
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.middleware.AuthenticationError
import zio.http.Method.POST
import zio.http.codec.HttpContentCodec
import zio.http.endpoint.{AuthType, Endpoint, orOutError}
import zio.http.{Response, Route, Status}
import zio.{Chunk, ZIO}

object CreateEntry:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateEntryCommand,
    InfraFailure | NotAuthorized | AuthenticationError,
    CreateEntryCommand.Response,
    AuthType.Bearer.type
  ] =
    Endpoint(POST / prefix / "entries")
      .tag("Vocabulary Entries")
      .in[CreateEntryCommand]
      .out[CreateEntryCommand.Response]
      .outError[InfraFailure](Status.InternalServerError)
      .orOutError[NotAuthorized](Status.Forbidden)
      .orOutError[AuthenticationError](Status.Unauthorized)
      .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[CreateEntryUseCase & UserContext, Response] =
    endpoint.implement(request =>
      ZIO.serviceWithZIO[CreateEntryUseCase](
        _(CreateEntryCommand(request.entry, Chunk.empty, 1L.identifier))
      )
    )
