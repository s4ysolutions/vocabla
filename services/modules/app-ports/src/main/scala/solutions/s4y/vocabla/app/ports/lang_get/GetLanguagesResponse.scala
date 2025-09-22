package solutions.s4y.vocabla.app.ports.lang_get

import solutions.s4y.vocabla.domain.Lang
import zio.Chunk
import zio.schema.annotation.description
import zio.schema.{DeriveSchema, Schema}

@description("Response containing the list of all languages, the default language, and the unknown language.")
final case class GetLanguagesResponse(defaultLang: Lang, unknownLang: Lang, languages: Chunk[Lang])

object GetLanguagesResponse:
  given Schema[GetLanguagesResponse] = DeriveSchema.gen[GetLanguagesResponse]
