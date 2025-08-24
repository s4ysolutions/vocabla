package solutions.s4y.vocabla.endpoint.http.rest.error

import zio.schema.{Schema, derived}

enum ErrorResponse:
  case ErrorService(message: String)
  case ErrorParseID(message: String)
  case ErrorUnknown(message: String)
  case AuthenticationError(message: String)

object ErrorResponse:
  given Schema[ErrorService] = Schema.derived
  given Schema[ErrorParseID] = Schema.derived
  given Schema[ErrorUnknown] = Schema.derived
  given Schema[AuthenticationError] = Schema.derived
