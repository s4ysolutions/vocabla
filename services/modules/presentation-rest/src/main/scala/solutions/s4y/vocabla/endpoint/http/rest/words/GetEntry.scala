package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.app.ports.{GetEntryCommand, GetEntryUseCase}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.ErrorService
import solutions.s4y.vocabla.endpoint.http.rest.prefix
import zio.http.Method.GET
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Response, Route, Status, long}
import zio.ZIO
/*
object GetEntry:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    Long,
    ErrorResponse.ErrorService,
    GetEntryCommand.Response,
    None
  ] =
    Endpoint(GET / prefix / "words" / "entries" / long("entryId"))
      .tag("Vocabulary Entries")
      .out[GetEntryCommand.Response]
      .outError[ErrorService](Status.InternalServerError)

  def route(using IdentifierSchema): Route[GetEntryUseCase, Response] =
    endpoint.implement { entryId =>
      ZIO
        .serviceWithZIO[GetEntryUseCase] {
          _.apply(GetEntryCommand(entryId.identifier[Entry]))
        }
        .mapError(error => ErrorService(error))
    }
 */
