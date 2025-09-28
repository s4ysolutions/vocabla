package solutions.s4y.vocabla.endpoint.http.routes.students.settings.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.app.ports.students.settings.tags.{CreateTagCommand, CreateTagResponse, CreateTagUseCase}
import solutions.s4y.vocabla.domain.User.Student
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.IdentifierSchema
import solutions.s4y.vocabla.domain.{Tag, UserContext}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.error.HttpError.{Forbidden403, InternalServerError500}
import solutions.s4y.vocabla.endpoint.http.middleware.BrowserLocale.withLocale
import solutions.s4y.vocabla.endpoint.http.routes.students.prefix
import solutions.s4y.vocabla.endpoint.http.routes.students.settings.openapiTag
import zio.ZIO
import zio.http.*
import zio.http.Method.POST
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

import java.util.Locale

object CreateTag:
  @description("Command to create a new tag.")
  final case class CreateTagRequest(
      @description(
        "The label of the tag to be created."
      )
      label: String
  )

  object CreateTagRequest:
    given (using IdentifierSchema): Schema[CreateTagRequest] = Schema.derived

  def endpoint(using
      IdentifierSchema
  ): Endpoint[
    Long,
    CreateTagCommand,
    HttpError,
    CreateTagResponse,
    AuthType.Bearer.type
  ] = Endpoint(POST / prefix / long("studentId") / "learning-settings" / "tags")
    .tag(openapiTag)
    .in[CreateTagRequest]
    .out[CreateTagResponse](Status.Created)
    .outErrors[HttpError](
      HttpCodec.error[InternalServerError500](Status.InternalServerError),
      HttpCodec.error[Forbidden403](Status.Forbidden)
    )
    .transformIn((studentId, request) =>
      CreateTagCommand(Tag(request.label, studentId.identifier[Student]))
    )(command =>
      (command.tag.ownerId.as[Long], CreateTagRequest(command.tag.label))
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
