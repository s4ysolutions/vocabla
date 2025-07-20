package solutions.s4y.vocabla.lang.infra

import solutions.s4y.vocabla.lang.app.repo.LangRepository
import solutions.s4y.vocabla.lang.domain.model.Lang

given langRoRepository: LangRepository with
  val defaultLang: Lang = Lang("en", "🇬🇧", "English")
  val unknownLang: Lang = Lang("unk", "❓", "Unknown")
  def getLangs: Seq[Lang] =
    Seq(
      defaultLang,
      Lang("fr", "🇫🇷", "French"),
      Lang("ru", "🇷🇺", "Russian"),
      Lang("es", "🇪🇸", "Spanish")
    )

  def getLang(
      code: String
  ): Lang = {
    getLangs.find(_.code == code).getOrElse(unknownLang)
  }
