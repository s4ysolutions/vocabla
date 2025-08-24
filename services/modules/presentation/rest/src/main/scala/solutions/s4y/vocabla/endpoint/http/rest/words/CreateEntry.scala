package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.app.ports.{CreateEntryCommand, CreateEntryUseCase}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.ErrorService
import zio.http.Method.POST
import zio.http.codec.HttpContentCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Response, Route, Status}
import zio.{Chunk, ZIO}

object CreateEntry:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateEntryCommand,
    ErrorResponse.ErrorService,
    CreateEntryCommand.Response,
    None
  ] =
    Endpoint(POST / prefix / "entries")
      .tag("Vocabulary Entries")
      .in[CreateEntryCommand]
      .out[CreateEntryCommand.Response]
      .outError[ErrorService](Status.InternalServerError)

  def route(using IdentifierSchema): Route[CreateEntryUseCase, Response] =
    endpoint.implement { request =>
      ZIO
        .serviceWithZIO[CreateEntryUseCase] {
          _.apply(CreateEntryCommand(request.entry, Chunk.empty, 1L.identifier))
        }
        .mapError(error => ErrorService(error))
    }
