package solutions.s4y.vocabla.endpoint.http.rest.error

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import zio.http.Status
import zio.http.codec.HttpCodecType.Content
import zio.http.codec.{HttpCodec, HttpCodecType}

object ErrorCodec:
  val errorInfra: HttpCodec[HttpCodecType.Status & Content, ServiceFailure] =
    HttpCodec.error[ServiceFailure](Status.InternalServerError)
end ErrorCodec
