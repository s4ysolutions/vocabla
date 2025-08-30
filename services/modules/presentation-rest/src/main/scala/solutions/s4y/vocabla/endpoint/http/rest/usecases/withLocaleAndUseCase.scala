package solutions.s4y.vocabla.endpoint.http.rest.usecases

import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.endpoint.http.rest.middleware.BrowserLocale.withLocale
import zio.ZIO
import zio.http.{Response, Route}
import zio.http.endpoint.Endpoint

import java.util.Locale

def implementWithUseCase[UC: zio.Tag, Cmd, Resp, E](
                                            endpoint: Endpoint[?, Cmd, E, Resp, ?]
                                          )(
                                            handler: UC => Cmd => ZIO[Any, E, Resp]
                                          ): Route[UC & Locale & UserContext, Response] =
  endpoint.implement { command =>
    withLocale {
      ZIO.serviceWithZIO[UC] { useCase =>
        handler(useCase)(command)
      }
    }
  }

def serviceWithLocaleY[U: zio.Tag, R, E, A](
    block: Locale ?=> U => ZIO[R, E, A]
): ZIO[U & Locale & R, E, A] =
  ZIO.serviceWithZIO[Locale](locale =>
    given Locale = locale
    for {
      useCase <- ZIO.service[U]
      result <- block(useCase)
    } yield result
  )

def serviceWithLocale[U: zio.Tag, R, E, A](
    block: Locale ?=> U => ZIO[R, E, A]
): ZIO[U & Locale & R, E, A] =
  ZIO.serviceWithZIO[Locale](locale =>
    given Locale = locale
    ZIO.serviceWith[U](block).flatten
  )

def serviceWithLocaleP[U: zio.Tag, E, A](
    block: U => Locale ?=> ZIO[UserContext, E, A]
): ZIO[U & Locale & UserContext, E, A] =
  ZIO.serviceWithZIO[Locale](locale =>
    given Locale = locale
    ZIO.serviceWithZIO[U](useCase => block(useCase)(using locale))
  )

def serviceWithLocaleX[U: zio.Tag, R, E, A](
    block: U => Locale ?=> ZIO[R, E, A]
): ZIO[U & Locale & R, E, A] =
  ZIO.serviceWithZIO[Locale](locale =>
    ZIO.serviceWithZIO[U](useCase => block(useCase)(using locale))
  )
/*
  (ZIO.service[Locale] <*> ZIO.service[U]).flatMap { (locale, useCase) =>
    block(useCase)(using locale)
  }
}
 */
/*
def serviceWithLocaleRequireExplicitRInCallSite[U: zio.Tag, R, E, A](
    block: U => Locale ?=> ZIO[R, E, A]
): ZIO[R & U & Locale, E, A] =
  (ZIO.service[Locale] <*> ZIO.service[U]).flatMap { (locale, useCase) =>
    block(useCase)(using locale)
  }
 */
