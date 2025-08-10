package solutions.s4y.vocabla.endpoint.http.rest.error

import zio.schema.{DeriveSchema, Schema}

enum ErrorResponse:
  case ErrorService(message: String)
  case ErrorParseID(message: String)
  case ErrorUnknown(message: String)

object ErrorResponse:
  given Schema[ErrorService] = DeriveSchema.gen[ErrorService]
  given Schema[ErrorParseID] = DeriveSchema.gen[ErrorParseID]
  given Schema[ErrorUnknown] = DeriveSchema.gen[ErrorUnknown]
