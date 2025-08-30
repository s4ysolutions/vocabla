package solutions.s4y.vocabla.app.repo.error

import solutions.s4y.i18n.TranslationTemplate
import zio.ZIO

import java.util.Locale

case class InfraFailure(message: TranslationTemplate, cause: Option[Throwable]):
  override def toString: String =
    cause match
      case Some(th) =>
        s"${message.toString(using Locale.ENGLISH)}: ${th.printStackTrace()}"
      case None => message.toString(using Locale.ENGLISH)

object InfraFailure:
  def apply(message: TranslationTemplate): InfraFailure =
    InfraFailure(message, None);
  extension [R, A](self: zio.ZIO[R, Throwable, A])
    def mapThrowable(
        message: TranslationTemplate
    ): zio.ZIO[R, InfraFailure, A] =
      self
        .tapErrorCause { cause =>
          ZIO.logErrorCause(message.toString(using Locale.ENGLISH), cause);
        }
        .mapError(th => InfraFailure(message, Option(th)))
