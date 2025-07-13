package solutions.s4y.vocabla.endpoint.http

import solutions.s4y.vocabla.endpoint.http.codecs.IdCodec
import solutions.s4y.vocabla.endpoint.http.rest.words.{
  entriesEndpoint,
  entriesRoute,
  newEntryEndpoint,
  newEntryRoute
}
import solutions.s4y.vocabla.endpoint.http.rest.{pingEndpoint, pingRoute}
import solutions.s4y.vocabla.words.app.usecase.WordsService
import zio.http.*
import zio.http.Header.AccessControlAllowOrigin
import zio.http.Middleware.CorsConfig
import zio.http.codec.PathCodec
import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen, SwaggerUI}
import zio.{LogLevel, Promise, Tag, Task, ULayer, ZIO, ZLayer, http}

class RESTService[
    DomainID: Tag,
    OwnerID: {IdCodec, Tag},
    EntryID: Tag,
    TagID: Tag
] {

  private val endpoints = Seq(pingEndpoint, newEntryEndpoint, entriesEndpoint)

  private val openAPI: OpenAPI = OpenAPIGen.fromEndpoints(
    title = "Vocabla API",
    version = "1.0.0",
    endpoints
  )

  private val restRoutes =
    Seq(pingRoute, newEntryRoute[DomainID, OwnerID, EntryID], entriesRoute)

  private val corsConfig: CorsConfig = CorsConfig(
    // allowedMethods = Acc
    // Some(Set(Method.GET, Method.POST, Method.PUT, Method.DELETE)),
    // allowedOrigin = _ => Some(AccessControlAllowOrigin.All)
    // allowedHeaders = Some(Set("Content-Type", "Authorization")),
    // exposedHeaders = Some(Set("Content-Type", "Authorization")),
    // allowCredentials = true,
    // maxAge = Some(3600)
  )
  private val routes =
    ((Routes.fromIterable(restRoutes) @@ Middleware.cors(
      corsConfig
    )) ++ SwaggerUI.routes(
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
    WordsService[DomainID, OwnerID, EntryID] & Server,
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
          fiber <- ZIO.never.fork
        } yield promise
      ) /*
      .provideSome[WordsService[DomainID, OwnerID, EntryID]](
        Server.default.mapError(th => th.toString)
      )*/
  }
}

object RESTService:
  def layer[
      DomainID: Tag,
      OwnerID: {IdCodec, Tag},
      EntryID: Tag,
      TagID: Tag
  ](): ULayer[RESTService[DomainID, OwnerID, EntryID, TagID]] =
    ZLayer.succeed(new RESTService[DomainID, OwnerID, EntryID, TagID])
