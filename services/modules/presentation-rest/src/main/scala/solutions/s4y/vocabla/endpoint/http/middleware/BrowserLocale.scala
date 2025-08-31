package solutions.s4y.vocabla.endpoint.http.middleware

import zio.http.Header.AcceptLanguage
import zio.http.Header.AcceptLanguage.Any
import zio.http.{Handler, HandlerAspect, Middleware, Request}
import zio.{Chunk, NonEmptyChunk, ZIO}

import java.util.Locale

object BrowserLocale:
  val browserLocale: HandlerAspect[Any, Locale] =
    Middleware.interceptIncomingHandler(
      Handler.fromFunctionZIO[Request] { request =>
        {
          val locale = extractLocale(request)
          ZIO.succeed((request, locale))
        }
      }
    )

  def withLocale[R, E, A](f: Locale ?=> ZIO[R, E, A]): ZIO[R & Locale, E, A] =
    ZIO.serviceWithZIO[Locale](locale => f(using locale))

  private def extractLocale(request: Request): Locale =
    request
      .header(AcceptLanguage)
      .flatMap(headerToLocale)
      .getOrElse(Locale.getDefault)

  private def headerToLocale(header: AcceptLanguage): Option[Locale] =
    collectSingles(header)
      .maxByOption(_.weight.getOrElse(1.0))
      .map(single => Locale.forLanguageTag(single.language))
      .filter(locale => locale != null && locale.toLanguageTag.nonEmpty)

  private def collectSingles(
      header: AcceptLanguage
  ): Chunk[AcceptLanguage.Single] =
    header match {
      case AcceptLanguage.Multiple(values) =>
        values.flatMap(collectSingles)
      case single: AcceptLanguage.Single =>
        NonEmptyChunk(single)
      case Any => Chunk.empty
    }
end BrowserLocale
