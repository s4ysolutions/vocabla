package solutions.s4y.vocabla.domain.identity

import zio.schema.Schema

trait IdentifierSchema:
  type ID
  val schema: Schema[ID]

object IdentifierSchema:
  def apply[I: Schema]: IdentifierSchema = new IdentifierSchema:
    type ID = I
    val schema: Schema[ID] = Schema[ID]

