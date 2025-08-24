package solutions.s4y.vocabla.endpoint.http.rest

import solutions.s4y.vocabla.app.ports.{PingCommand, PingUseCase}
import zio.http.codec.HttpCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Method, Response, Route}
import zio.schema.validation.Validation
import zio.schema.{DeriveSchema, Schema}
import zio.{ZIO, ZNothing, durationInt}

object Ping:
  val endpoint
      : Endpoint[Unit, PingCommand, Nothing, PingCommand.Response, None] =
    Endpoint(Method.GET / prefix / "ping")
      .query(HttpCodec.query[PingCommand])
      .out[PingCommand.Response]

  val route: Route[PingUseCase, Response] =
    endpoint.implement(request =>
      ZIO
        .serviceWithZIO[PingUseCase](_(request))
        .mapError(s => Exception(s))
        .orDie
    )
