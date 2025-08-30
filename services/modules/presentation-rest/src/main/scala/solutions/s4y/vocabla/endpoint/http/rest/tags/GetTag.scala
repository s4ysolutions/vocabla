package solutions.s4y.vocabla.endpoint.http.rest.tags

import solutions.s4y.vocabla.app.ports.{GetTagCommand, GetTagUseCase}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse
import solutions.s4y.vocabla.endpoint.http.rest.error.ErrorResponse.ErrorService
import solutions.s4y.vocabla.endpoint.http.rest.prefix
import zio.http.Method.GET
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Response, Route, Status, long}
import zio.ZIO

object GetTag:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    Long,
    ErrorResponse.ErrorService,
    GetTagCommand.Response,
    None
  ] =
    Endpoint(GET / prefix / "tags" / long("tagId"))
      .tag("Tags")
      .out[GetTagCommand.Response]
      .outError[ErrorService](Status.InternalServerError)

  def route(using IdentifierSchema): Route[GetTagUseCase, Response] =
    endpoint.implement { tagId =>
      ZIO
        .serviceWithZIO[GetTagUseCase] {
          _.apply(GetTagCommand(tagId.identifier[Tag]))
        }
        .mapError(error => ErrorService(error))
    }
