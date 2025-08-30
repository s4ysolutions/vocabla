package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.{CreateEntryCommand, CreateEntryUseCase}
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError.{
  Forbidden403,
  InternalServerError500,
  NotAuthorized401
}
import solutions.s4y.vocabla.endpoint.http.rest.middleware.AuthenticationError
import zio.http.Method.POST
import zio.http.codec.{HttpCodec, HttpContentCodec}
import zio.http.endpoint.{AuthType, Endpoint, orOutError}
import zio.http.{Response, Route, Status}
import zio.schema.{DeriveSchema, Schema, TypeId}
import zio.{Chunk, NonEmptyChunk, ZIO}

import java.util.Locale
/*
object CreateEntry:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateEntryCommand,
    NotAuthorized401 | Forbidden403 | InternalServerError500,
    CreateEntryCommand.Response,
    AuthType.Bearer.type
  ] =
    Endpoint(POST / prefix / "entries")
      .tag("Vocabulary Entries")
      .in[CreateEntryCommand]
      .out[CreateEntryCommand.Response]
      .outError[NotAuthorized401](Status.Unauthorized)
      .orOutError[Forbidden403](Status.Forbidden)
      .orOutError[InternalServerError500](Status.InternalServerError)
      .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[
    CreateEntryUseCase & UserContext & Locale,
    NotAuthorized401 | Forbidden403 | InternalServerError500
  ] =
    endpoint.implement(request =>
      for {
        locale <- ZIO.service[Locale]
        r <- ZIO.serviceWithZIO[CreateEntryUseCase](
          _(CreateEntryCommand(request.entry, Chunk.empty, 1L.identifier))
            .mapError(
              given Locale = locale
              e match {
                case m: NotAuthorized =>
                  Error[NotAuthorized](
                    m.messages.map(_.toString)
                  )
                case m: ServiceFailure =>
                  Error[ServiceFailure](NonEmptyChunk(m.message))
              }
            )
        )
      } yield r
    )
*/