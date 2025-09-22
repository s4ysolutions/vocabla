package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Lang
import zio.Chunk

trait LangRepository:
  val defaultLang: Lang
  val unknownLang: Lang
  def getLangs: Chunk[Lang]
  def getLang(code: Lang.Code): Lang
