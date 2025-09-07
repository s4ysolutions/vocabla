package solutions.s4y.vocabla.endpoint.http.routes.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.tag_get.{GetTagRequest, GetTagResponse, GetTagUseCase}
import solutions.s4y.vocabla.app.ports.GetEntryCommand
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{Entry, Tag, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{Forbidden403, InternalServerError500}
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
    GetTagRequest,
    HttpError,
    GetTagResponse,
    AuthType.Bearer.type
  ] =
    Endpoint(GET / prefix / long("tagId"))
      .tag("Tags")
      .out[GetTagResponse]
      .outErrors[HttpError](
        HttpCodec.error[InternalServerError500](Status.InternalServerError),
        HttpCodec.error[Forbidden403](Status.Forbidden)
      )
      .transformIn(id => GetTagRequest(id.identifier[Tag]))(command =>
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
