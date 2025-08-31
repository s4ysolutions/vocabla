package solutions.s4y.vocabla.endpoint.http.rest.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.{
  GetEntryCommand,
  GetTagCommand,
  GetTagUseCase
}
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{Entry, Tag, UserContext}
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
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Response, Route, Status, long}

import java.util.Locale

object GetTag:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    GetTagCommand,
    HttpError,
    GetTagCommand.Response,
    AuthType.Bearer.type
  ] =
    Endpoint(GET / prefix / "tags" / long("tagId"))
      .tag("Tags")
      .out[GetTagCommand.Response]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .transformIn(id => GetTagCommand(id.identifier[Tag]))(command =>
        command.tagId.as[Long]
      )
      .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[GetTagUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[GetTagUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }
