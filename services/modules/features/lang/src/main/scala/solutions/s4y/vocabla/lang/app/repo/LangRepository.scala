package solutions.s4y.vocabla.lang.app.repo

import solutions.s4y.vocabla.lang.domain.model.Lang

trait LangRepository:
  def getLangs: Seq[Lang]
  def getLang(code: String): Lang
