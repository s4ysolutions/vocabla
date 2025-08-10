package solutions.s4y.vocabla.domain.entry

import solutions.s4y.vocabla.domain.Lang
import zio.schema.{DeriveSchema, Schema}

final case class Definition(definition: String, langCode: Lang.Code):
  override def toString: String = s"Definition ($langCode) $definition"

object Definition:
  given Schema[Definition] =
    DeriveSchema.gen[Definition]
