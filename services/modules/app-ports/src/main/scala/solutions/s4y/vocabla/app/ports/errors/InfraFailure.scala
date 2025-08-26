package solutions.s4y.vocabla.app.ports.errors

import zio.schema.{Schema, derived}

final case class InfraFailure(message: String)

object InfraFailure:
  given Schema[InfraFailure] = Schema.derived
