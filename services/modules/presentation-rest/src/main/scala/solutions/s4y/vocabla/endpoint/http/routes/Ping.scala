package solutions.s4y.vocabla.endpoint.http.routes

import solutions.s4y.vocabla.app.ports.{PingCommand, PingUseCase}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import zio.ZIO
import zio.http.codec.{Doc, HttpCodec}
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Method, Response, Route, Status}
import zio.schema.Schema

object Ping:
  val endpoint: Endpoint[
    Unit,
    PingCommand,
    HttpError.BadRequest400,
    PingCommand.Response,
    None
  ] =
    Endpoint(
      (Method.GET / prefix / "ping") ?? Doc.p(
        "Diagnostic endpoint to check service health"
      )
    )
      .tag("Diagnostics")
      .query(
        HttpCodec
          .query[PingCommand]
          .examples("example" -> PingCommand("a payload")) ?? Doc.p(
          "The payload message to echo back. Must be at least 2 characters long."
        )
      )
      .out[PingCommand.Response](Doc.p("The echoed payload"))
      .outError[HttpError.BadRequest400](Status.BadRequest)
      ?? Doc
        .p("Ping endpoint that echoes back the provided payload")

  val route: Route[PingUseCase, Response] =
    endpoint.implement(request =>
      ZIO
        .serviceWithZIO[PingUseCase](_(request))
        .mapError(s => Exception(s))
        .orDie
    )
