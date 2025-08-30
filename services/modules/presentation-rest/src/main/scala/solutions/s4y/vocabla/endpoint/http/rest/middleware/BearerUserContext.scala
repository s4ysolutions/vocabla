package solutions.s4y.vocabla.endpoint.http.rest.middleware

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.vocabla.app.ports.GetUserUseCase
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{User, UserContext}
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError
import solutions.s4y.vocabla.endpoint.http.rest.error.HttpError.*
import solutions.s4y.vocabla.endpoint.http.rest.middleware.BrowserLocale.withLocale
import zio.ZIO
import zio.http.*

import java.util.Locale

object BearerUserContext:
  val bearerAuthWithContext
      : HandlerAspect[GetUserUseCase & Locale, UserContext] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] {
      request =>
        (request.header(Header.Authorization) match {
          case Some(Header.Authorization.Bearer(token)) =>
            for {
              id <- ZIO
                .attempt(token.stringValue.toLong)
                .tapErrorCause(th => ZIO.logWarning(th.prettyPrint))
                .mapError(th =>
                  NotAuthorized401(
                    s"Invalid token: ${token.stringValue} (${th.toString})"
                  )
                )
              userOpt <- ZIO
                .serviceWithZIO[GetUserUseCase](_(id.identifier[User]))
                .mapError(err => InternalServerError500(err.toString))
              user <- ZIO
                .fromOption(userOpt)
                .orElseFail(
                  NotFound404(s"User not found: $id")
                )
            } yield (request, UserContext(id.identifier[User], user))
          case _ =>
            ZIO.fail(
              NotAuthorized401(s"Authorization header is missing")
            )
        }).mapError {
          case NotAuthorized401(message) =>
            Response
              .unauthorized(message)
              .addHeader(
                Header.WWWAuthenticate
                  .Bearer("vocabla", None, Some(message))
              )
          case NotFound404(message) =>
            Response.error(Status.NotFound, message)
        }
    })

end BearerUserContext
