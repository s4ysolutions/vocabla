package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.lang.domain.model.Lang
import zio.schema.{DeriveSchema, Schema}

case class Definition(definition: String, lang: Lang) {
  override def toString: String = s"Definition ($lang) $definition"
}

object Definition:
  given Schema[Definition] =
    DeriveSchema.gen[Definition]
