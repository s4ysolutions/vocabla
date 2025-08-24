package solutions.s4y.vocabla.endpoint.http.rest.auth

import zio.ZIO
import zio.http.{Handler, HandlerAspect, Header, Headers, Request, Response}

object Authentication:
  val bearerAuthWithContext: HandlerAspect[Any, UserContext] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] {
      request =>
        request.header(Header.Authorization) match {
          case Some(Header.Authorization.Bearer(token)) =>
            ZIO
              .attempt((request, UserContext(token.stringValue.toLong)))
              .mapError(th =>
                Response.badRequest(
                  "Invalid or expired token: " + th.getMessage
                )
              )
          case _ =>
            ZIO.fail(
              Response.unauthorized.addHeaders(
                Headers(Header.WWWAuthenticate.Bearer(realm = "Access"))
              )
            )
        }
    })
