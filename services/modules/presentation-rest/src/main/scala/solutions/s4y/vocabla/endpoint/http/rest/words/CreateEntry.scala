package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.{
  CreateEntryCommand,
  CreateEntryUseCase,
  CreateTagUseCase
}
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError.{
  Forbidden403,
  InternalServerError500,
  NotAuthorized401
}
import solutions.s4y.vocabla.endpoint.http.rest.middleware.AuthenticationError
import solutions.s4y.vocabla.endpoint.http.rest.middleware.BrowserLocale.withLocale
import zio.http.Method.POST
import zio.http.codec.{HttpCodec, HttpContentCodec}
import zio.http.endpoint.{AuthType, Endpoint, orOutError}
import zio.http.{Response, Route, Status}
import zio.schema.{DeriveSchema, Schema, TypeId}
import zio.{Chunk, NonEmptyChunk, ZIO}

import java.util.Locale

object CreateEntry:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateEntryCommand,
    HttpError,
    CreateEntryCommand.Response,
    AuthType.Bearer.type
  ] =
    Endpoint(POST / prefix / "entries")
      .tag("Vocabulary Entries")
      .in[CreateEntryCommand]
      .out[CreateEntryCommand.Response]
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
            case e: NotAuthorized => Forbidden403(e.message.toString)
            case e: ServiceFailure =>
              InternalServerError500(e.message.toString)
          }
        )
      }
    )
