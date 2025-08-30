package solutions.s4y.vocabla.endpoint.http.rest.error

import zio.NonEmptyChunk
import zio.schema.{Schema, derived}

enum HttpError:
  case BadRequest400(messages: NonEmptyChunk[String]) extends HttpError
  case NotAuthorized401(message: String) extends HttpError
  case Forbidden403(message: String) extends HttpError
  case NotFound404(message: String) extends HttpError
  case Conflict409(message: String) extends HttpError
  case Gone410(message: String) extends HttpError
  case UnprocessableEntity422(message: NonEmptyChunk[String])
      extends HttpError
  case InternalServerError500(message: String) extends HttpError
  case NotImplemented501(message: String) extends HttpError
  case ServiceUnavailable503(message: String) extends HttpError
  case GatewayTimeout504(message: String) extends HttpError

object NotAuthorized401:
  import HttpError.NotAuthorized401
  given Schema[NotAuthorized401] = Schema.derived
