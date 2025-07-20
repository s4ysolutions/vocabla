package solutions.s4y.vocabla.endpoint

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.endpoint.http.RESTService
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.words.app.repo.DtoIdToDomainId
import solutions.s4y.vocabla.words.app.usecase.{
  WordsService,
  WordsServiceMVStore
}
import zio.http.Server
import zio.logging.LogFilter.LogLevelByNameConfig
import zio.logging.{ConsoleLoggerConfig, LogFormat, consoleLogger, logMetrics}
import zio.{
  Config,
  LogLevel,
  Runtime,
  Scope,
  ULayer,
  ZIO,
  ZIOAppArgs,
  ZIOAppDefault,
  ZLayer
}

import java.util.UUID

object Main extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Config.Error, Unit] =
    // Replace the default logger with the custom one
    Runtime.removeDefaultLoggers >>>
      consoleLogger(
        ConsoleLoggerConfig(
          LogFormat.colored + LogFormat.space + LogFormat.allAnnotations,
          LogLevelByNameConfig(LogLevel.Trace)
        )
      )

  type ID = UUID // use for mv store ids
  // id generator
  private def layerIdFactory: ULayer[IdFactory[ID]] =
    ZLayer.succeed(IdFactory.uuid)

  // create in-memory MV Store
  private def layerMVStore: ZLayer[Any, String, MVStore] =
    ZLayer.scoped(
      makeMVStoreMemory().mapError(th =>
        s"Failed to create MVStore: ${th.getMessage}"
      )
    )

  import solutions.s4y.vocabla.domain.model.Identifier.uuidConvertor
  import solutions.s4y.vocabla.lang.infra.langRoRepository
  
  // finally end up with WordsService implementation
  private val wordsServiceLayer: ZLayer[Any, String, WordsService] =
    (layerIdFactory ++ layerMVStore) >>> WordsServiceMVStore.makeLayer[ID]

  private val program: ZIO[
    Scope & WordsService & RESTService[ID, ID, ID, ID] & Server,
    String,
    Unit
  ] = {
    for {
      _ <- ZIO.logDebug("Starting app")
      restService <- ZIO.service[RESTService[ID, ID, ID, ID]]
      startedPromise <- restService.start()
      _ <- startedPromise.await
      _ <- ZIO.logInfo("Press Ctrl-C to stop the server")
      _ <- ZIO.addFinalizer(ZIO.logInfo("Cleaning up resources..."))
      _ <- ZIO.never
    } yield ()
  }

  override def run: ZIO[Scope, String, Unit] =
    program.provideSome[Scope](
      // TODO: should go into RESTService layer
      Server.default.mapError(th => th.toString) ++
        RESTService.layer[ID, ID, ID, ID]() ++ wordsServiceLayer
    )

  // Server.default.mapError(th => th.toString)
