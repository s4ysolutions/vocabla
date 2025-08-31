package solutions.s4y.vocabla.endpoint.http

import zio.config.*
import zio.{Config, ZIO, ZLayer}

case class RestConfig(port: Int = 8080)

object RestConfig:
  private val descriptor: Config[RestConfig] =
    Config
      .int("port")
      .withDefault(8080)
      .nested("rest")
      .to[RestConfig]

  val layer: ZLayer[Any, Nothing, RestConfig] = ZLayer.fromZIO(
    ZIO.logDebug("Loading REST config") *>
      ZIO.config(RestConfig.descriptor).orDie
  )
