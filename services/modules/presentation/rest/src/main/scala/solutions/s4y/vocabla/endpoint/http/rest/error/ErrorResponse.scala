package solutions.s4y.vocabla.endpoint.http.rest.error

import solutions.s4y.vocabla.app.ports.errors.InfraFailure
import zio.NonEmptyChunk
import zio.http.Status
import zio.http.codec.HttpCodecType.Content
import zio.http.codec.{HttpCodec, HttpCodecType}
import zio.schema.{Schema, derived}

enum ErrorResponse:
  case ErrorService(message: String)
  case ErrorParseID(message: String)
  case ErrorUnknown(message: String)
  case AuthenticationError(message: String)
  case AuthorizationErrors(messages: NonEmptyChunk[String])
end ErrorResponse

object ErrorResponse:
  given Schema[ErrorService] = Schema.derived
  given Schema[ErrorParseID] = Schema.derived
  given Schema[ErrorUnknown] = Schema.derived
  given Schema[AuthenticationError] = Schema.derived
  given Schema[AuthorizationErrors] = Schema.derived

end ErrorResponse
