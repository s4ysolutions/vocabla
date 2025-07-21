package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.lang.domain.model.Lang
import zio.schema.{DeriveSchema, Schema}

final case class Definition(definition: String, langCode: Lang.Code) {
  override def toString: String = s"Definition ($langCode) $definition"
}

object Definition:
  given Schema[Definition] =
    DeriveSchema.gen[Definition]
