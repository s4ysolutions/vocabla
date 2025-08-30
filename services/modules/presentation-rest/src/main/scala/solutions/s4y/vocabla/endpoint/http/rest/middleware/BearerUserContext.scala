package solutions.s4y.vocabla.endpoint.http.rest.middleware

import solutions.s4y.vocabla.app.ports.GetUserUseCase
import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{User, UserContext}
import zio.ZIO
import zio.http.*

object BearerUserContext:
  val bearerAuthWithContext: HandlerAspect[GetUserUseCase, UserContext] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] {
      request =>
        (request.header(Header.Authorization) match {
          case Some(Header.Authorization.Bearer(token)) =>
            for {
              id <- ZIO
                .attempt(token.stringValue.toLong)
                .tapErrorCause(th => ZIO.logWarning(th.prettyPrint))
                .mapError(th =>
                  AuthenticationError(
                    s"Invalid token: ${token.stringValue} (${th.toString})"
                  )
                )
              userOpt <- ZIO
                .serviceWithZIO[GetUserUseCase](_(id.identifier[User]))
                .mapError(err => ServiceFailure(err))
              user <- ZIO
                .fromOption(userOpt)
                .orElseFail(AuthenticationError("User not found: " + id))
            } yield (request, UserContext(id.identifier[User], user))
          case _ =>
            ZIO.fail(AuthenticationError("Authorization header is missing"))
        }).mapError {
          case AuthenticationError(message) =>
            Response
              .unauthorized(message)
              .contentType(MediaType.text.plain)
              .addHeader(
                Header.WWWAuthenticate
                  .Bearer("vocabla", None, Some(message))
              )
          case ServiceFailure(message) =>
            Response
              .internalServerError(message)
              .contentType(MediaType.text.plain)
        }
    })
end BearerUserContext
