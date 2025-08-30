package solutions.s4y.vocabla.app.ports.errors

import solutions.s4y.i18n.TranslationTemplate

import java.util.Locale

final case class ServiceFailure(
    message: TranslationTemplate,
    cause: Option[Throwable] = None
):
  override def toString: String =
    message.toString(using Locale.ENGLISH)
