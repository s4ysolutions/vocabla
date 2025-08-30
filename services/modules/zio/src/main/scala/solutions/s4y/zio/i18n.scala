package solutions.s4y.zio

import solutions.s4y.i18n.TranslationTemplate
import zio.ZIO

import java.util.Locale

extension (tt: TranslationTemplate)
  def toStringZIO: ZIO[Locale, Nothing, String] = ZIO.serviceWith[Locale] {
    locale =>
      tt.toString(using locale)
  }
