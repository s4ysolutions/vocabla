package solutions.s4y.vocabla.endpoint.http.rest

import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint
import zio.http.{Method, Route}
import zio.schema.annotation.validate
import zio.schema.validation.Validation
import zio.schema.{DeriveSchema, Schema}
import zio.{ZIO, durationInt}

private case class PingRequest(
    @validate(Validation.minLength(2)) payload: String
)

private object PingRequest:
  given Schema[PingRequest] = DeriveSchema.gen[PingRequest]

private case class PongResponse(payload: String)

private object PongResponse:
  given Schema[PongResponse] = DeriveSchema.gen[PongResponse]

val pingEndpoint = Endpoint(Method.GET / prefix / "ping")
  .query(HttpCodec.query[PingRequest])
  .out[PongResponse]

val pingRoute: Route[Any, Nothing] =
  pingEndpoint.implement(request =>
    ZIO
      .sleep(200.millis)
      .as(PongResponse(s"pong: ${request.payload}"))
  )
