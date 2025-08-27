package solutions.s4y.i18n

import zio.prelude.Validation

import java.util.Locale
import scala.util.{Failure, Success, Try}

object TranslationOps:

  type TranslationError = String

  def validateTemplate(template: TranslationTemplate)(using resolver: TranslationResolver, locale: Locale): Validation[TranslationError, String] =
    Try(template.toString) match
      case Success(result) => Validation.succeed(result)
      case Failure(ex) => Validation.fail(s"Translation failed: ${ex.getMessage}")
/*
  def combineTranslations(templates: TranslationTemplate*)(separator: String = " ")(using resolver: TranslationResolver, locale: Locale): Validation[TranslationError, String] =
    templates.toList.map(validateTemplate).reduceOption(_ <*> _) match
      case Some(validation) => validation.map(_.mkString(separator))
      case None => Validation.succeed("")
*/
end TranslationOps
