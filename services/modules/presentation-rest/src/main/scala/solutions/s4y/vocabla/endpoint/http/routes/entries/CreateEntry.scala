package solutions.s4y.vocabla.endpoint.http.routes.entries

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.entry_create.{CreateEntryRequest, CreateEntryResponse, CreateEntryUseCase}
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{Forbidden403, InternalServerError500}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import zio.ZIO
import zio.http.Method.POST
import zio.http.codec.{HttpCodec, HttpContentCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Response, Route, Status}

import java.util.Locale

object CreateEntry:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateEntryRequest,
    HttpError,
    CreateEntryResponse,
    AuthType.Bearer.type
  ] =
    Endpoint(POST / prefix)
      .tag("Vocabulary Entries")
      .in[CreateEntryRequest]
      .out[CreateEntryResponse]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[CreateEntryUseCase & UserContext & Locale, Response] =
    endpoint.implement(command =>
      withLocale {
        ZIO.serviceWithZIO[CreateEntryUseCase](useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        )
      }
    )
