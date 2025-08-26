package solutions.s4y.vocabla.endpoint.http.rest.middleware

import zio.schema.{Schema, derived}

final case class AuthenticationError(message: String)

object AuthenticationError:
  given Schema[AuthenticationError] = Schema.derived
