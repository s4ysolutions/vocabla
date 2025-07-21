package solutions.s4y.vocabla.domain.model

import zio.schema.Schema

trait IdentifierSchema:
  type ID
  val schema: Schema[ID]

object IdentifierSchema:
  def apply[I: Schema]: IdentifierSchema = new IdentifierSchema:
    type ID = I
    val schema: Schema[I] = Schema[ID]
