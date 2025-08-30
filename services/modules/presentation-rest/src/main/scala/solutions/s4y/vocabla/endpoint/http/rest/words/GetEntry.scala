package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.{GetEntryCommand, GetEntryUseCase}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Entry, UserContext}
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError.{
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.rest.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.rest.prefix
import zio.ZIO
import zio.http.Method.GET
import zio.http.codec.{HttpCodec, PathCodec}
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Response, Route, Status, long}

import java.util.Locale

object GetEntry:
  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    GetEntryCommand,
    HttpError,
    GetEntryCommand.Response,
    None
  ] =
    Endpoint(GET / prefix / "words" / "entries" / long("entryId"))
      .tag("Vocabulary Entries")
      .out[GetEntryCommand.Response]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .transformIn(entryId => GetEntryCommand(entryId.identifier[Entry]))(
        command => command.entryId.as[Long]
      )

  def route(using IdentifierSchema): Route[GetEntryUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[GetEntryUseCase] { useCase =>
          useCase(command).mapError {
            //case e: NotAuthorized => Forbidden403(e.message.toString)
            case e: ServiceFailure =>
              InternalServerError500(e.message.toString)
          }
        }
      }
    }
