package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Lang

trait LangRepository:
  def getLangs: Seq[Lang]
  def getLang(code: Lang.Code): Lang
