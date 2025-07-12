package solutions.s4y.vocabla.endpoint.http

import solutions.s4y.vocabla.endpoint.http.codecs.IdCodec
import solutions.s4y.vocabla.endpoint.http.rest.words.{
  newEntryEndpoint,
  newEntryRoute
}
import solutions.s4y.vocabla.endpoint.http.rest.{pingEndpoint, pingRoute}
import solutions.s4y.vocabla.words.app.usecase.WordsService
import zio.http.*
import zio.http.codec.PathCodec
import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen, SwaggerUI}
import zio.{LogLevel, Promise, Tag, Task, ULayer, ZIO, ZLayer, http}

class RESTServer[
    DomainID: Tag,
    OwnerID: {IdCodec, Tag},
    EntryID: Tag,
    TagID: Tag
] {

  private val endpoints = Seq(pingEndpoint, newEntryEndpoint)

  private val openAPI: OpenAPI = OpenAPIGen.fromEndpoints(
    title = "Vocabla API",
    version = "1.0.0",
    endpoints
  )

  private val restRoutes =
    Seq(pingRoute, newEntryRoute[DomainID, OwnerID, EntryID])

  private val routes =
    (Routes.fromIterable(restRoutes) ++ SwaggerUI.routes(
      "/openapi",
      openAPI
    )) @@ Middleware
      .requestLogging(level =
        status =>
          if (status.isSuccess) {
            LogLevel.Info
          } else {
            LogLevel.Error
          }
      )

  def start(): ZIO[
    WordsService[DomainID, OwnerID, EntryID],
    String,
    Promise[Nothing, Unit]
  ] = {
    Server
      .install(routes)
      .flatMap(port =>
        for {
          promise <- Promise.make[Nothing, Unit]
          _ <- ZIO.log(s"Server started on http://localhost:$port") //  ZIO.never
          _ <- promise.succeed(())
          _ <- ZIO.never.fork
        } yield promise
      )
      .provideSome[WordsService[DomainID, OwnerID, EntryID]](
        Server.default.mapError(th => th.toString)
      )
  }
}

object RESTServer:
  def layer[
      DomainID: Tag,
      OwnerID: {IdCodec, Tag},
      EntryID: Tag,
      TagID: Tag
  ](): ULayer[RESTServer[DomainID, OwnerID, EntryID, TagID]] =
    ZLayer.succeed(new RESTServer[DomainID, OwnerID, EntryID, TagID])
