package solutions.s4y.vocabla.endpoint.http.rest

import zio.http.codec.HttpCodec
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Method, Route}
import zio.schema.annotation.validate
import zio.schema.validation.Validation
import zio.schema.{DeriveSchema, Schema}
import zio.{ZIO, ZNothing, durationInt}

case class PingRequest(
    @validate(Validation.minLength(2)) payload: String
)
case class PingResponse(payload: String)

object Ping:
  val endpoint: Endpoint[Unit, PingRequest, ZNothing, PingResponse, None] =
    Endpoint(Method.GET / prefix / "ping")
      .query(HttpCodec.query[PingRequest])
      .out[PingResponse]

  val route: Route[Any, Nothing] =
    endpoint.implement(request =>
      ZIO
        .sleep(200.millis)
        .as(PingResponse(s"pong: ${request.payload}"))
    )

  private given Schema[PingRequest] = DeriveSchema.gen[PingRequest]
  private given Schema[PingResponse] = DeriveSchema.gen[PingResponse]
