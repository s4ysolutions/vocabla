package solutions.s4y.vocabla.endpoint.http.rest.auth

import solutions.s4y.vocabla.app.ports.GetUserUseCase
import solutions.s4y.vocabla.domain.User
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import zio.ZIO
import zio.http.*

object Authentication:
  val bearerAuthWithContext: HandlerAspect[GetUserUseCase, UserContext] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] {
      request =>
        request.header(Header.Authorization) match {
          case Some(Header.Authorization.Bearer(token)) =>
            for {
              id <- ZIO
                .attempt(token.stringValue.toLong)
                .tapErrorCause(th => ZIO.logWarning(th.prettyPrint))
                .mapError(th =>
                  Response.badRequest(
                    s"Invalid token: ${token.stringValue} (${th.toString})"
                  )
                )
              userOpt <- ZIO
                .serviceWithZIO[GetUserUseCase](_(id.identifier[User]))
                .mapError(e => Response.internalServerError(e))
              user <- ZIO
                .fromOption(userOpt)
                .orElseFail(Response.unauthorized("User not found: " + id))
            } yield (request, UserContext(id.identifier[User], user))
          case _ =>
            ZIO.fail(Response.unauthorized("Authorization header is missing"))
        }
    })
