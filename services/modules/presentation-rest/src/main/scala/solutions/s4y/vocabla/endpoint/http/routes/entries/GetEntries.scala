package solutions.s4y.vocabla.endpoint.http.routes.entries

import solutions.s4y.vocabla.app.ports.entries_get.{
  GetEntriesRequest,
  GetEntriesResponse,
  GetEntriesUseCase
}
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Tag, User, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{
  BadRequest400,
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import zio.{Chunk, ZIO}
import zio.http.Method.GET
import zio.http.codec.HttpCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Response, Route, Status}

import java.util.Locale

object GetEntries:
  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    GetEntriesRequest,
    HttpError,
    GetEntriesResponse,
    None
  ] =
    Endpoint(GET / prefix)
      .tag("Vocabulary Entries")
      .query(
        HttpCodec
          .query[GetEntriesRequest] /*
          .examples(
            "example" -> GetEntriesRequest(
              studentId = Some(123L.identifier[User]),
              tagId = Chunk(456L.identifier[Tag], 789L.identifier[Tag]),
              lang = Chunk("en", "es"),
              text = Some("search term")
            )
          )*/
      )
      .out[GetEntriesResponse]
      .outErrors[HttpError](
        HttpCodec.error[BadRequest400](Status.BadRequest),
        HttpCodec.error[Forbidden403](Status.Forbidden),
        HttpCodec.error[InternalServerError500](Status.InternalServerError)
      )

  def route(using
      IdentifierSchema
  ): Route[GetEntriesUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[GetEntriesUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }

end GetEntries
