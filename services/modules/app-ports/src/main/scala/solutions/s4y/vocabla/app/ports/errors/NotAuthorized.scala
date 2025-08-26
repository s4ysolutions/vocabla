package solutions.s4y.vocabla.app.ports.errors

import zio.NonEmptyChunk
import zio.schema.{Schema, derived}

final case class NotAuthorized(messages: NonEmptyChunk[String])

object NotAuthorized:
  given Schema[NotAuthorized] = Schema.derived
