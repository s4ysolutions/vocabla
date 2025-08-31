package solutions.s4y.vocabla.endpoint.http.rest.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.{CreateTagCommand, CreateTagUseCase}
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError.{
  Forbidden403,
  InternalServerError500
}
import solutions.s4y.vocabla.endpoint.http.rest.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.rest.prefix
import zio.ZIO
import zio.http.*
import zio.http.Method.POST
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}

import java.util.Locale

object CreateTag:

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Unit,
    CreateTagCommand,
    HttpError,
    CreateTagCommand.Response,
    AuthType.Bearer.type
  ] = Endpoint(POST / prefix / "tags")
    .tag("Tags")
    .in[CreateTagCommand]
    .out[CreateTagCommand.Response]
    .outErrors[HttpError](
      HttpCodec.error[InternalServerError500](Status.InternalServerError),
      HttpCodec.error[Forbidden403](Status.Forbidden)
    )
    .auth(AuthType.Bearer)

  def route(using
      IdentifierSchema
  ): Route[CreateTagUseCase & Locale & UserContext, Response] =
    endpoint.implement { command =>
      withLocale {
        ZIO.serviceWithZIO[CreateTagUseCase] { useCase =>
          useCase(command).mapError {
            case e: NotAuthorized => Forbidden403(e.message.localized)
            case e: ServiceFailure =>
              InternalServerError500(e.message.localized)
          }
        }
      }
    }
