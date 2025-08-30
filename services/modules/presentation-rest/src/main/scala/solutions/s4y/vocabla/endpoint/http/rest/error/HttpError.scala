package solutions.s4y.vocabla.endpoint.http.rest.error

import zio.NonEmptyChunk
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

enum HttpError derives Schema:
  @description(
    "The request could not be understood or was missing required parameters."
  ) case BadRequest400(
      message: String
  ) extends HttpError
  @description(
    "Authentication is required and has failed or has not yet been provided."
  ) case NotAuthorized401(
      message: String
  ) extends HttpError
  @description(
    "You do not have permission to access this resource."
  ) case Forbidden403(
      message: String
  ) extends HttpError
  @description("The requested resource could not be found.") case NotFound404(
      message: String
  ) extends HttpError
  @description(
    "A conflict occurred with the current state of the resource."
  ) case Conflict409(
      message: String
  ) extends HttpError
  @description(
    "The requested resource is no longer available and will not be available again."
  ) case Gone410(message: String) extends HttpError
  @description(
    "The input data is invalid or cannot be processed."
  ) case UnprocessableEntity422(
      message: NonEmptyChunk[String]
  ) extends HttpError
  @description("Internal server error occurred") case InternalServerError500(
      message: String
  ) extends HttpError
  @description(
    "The requested functionality is not implemented."
  ) case NotImplemented501(
      message: String
  ) extends HttpError
  @description(
    "The server is currently unavailable (overloaded or down)."
  ) case ServiceUnavailable503(
      message: String
  ) extends HttpError
  @description(
    "The server did not receive a timely response from an upstream server."
  ) case GatewayTimeout504(
      message: String
  ) extends HttpError

object HttpError:
  given Schema[BadRequest400] = Schema.derived
  given Schema[NotAuthorized401] = Schema.derived
  given Schema[Forbidden403] = Schema.derived
  given Schema[NotFound404] = Schema.derived
  given Schema[Conflict409] = Schema.derived
  given Schema[Gone410] = Schema.derived
  given Schema[UnprocessableEntity422] = Schema.derived
  given Schema[InternalServerError500] = Schema.derived
  given Schema[NotImplemented501] = Schema.derived
  given Schema[ServiceUnavailable503] = Schema.derived
  given Schema[GatewayTimeout504] = Schema.derived
