package solutions.s4y.vocabla.endpoint

import org.h2.mvstore.MVStore
import solutions.s4y.vocabla.domain.model.IdentifierSchema
import solutions.s4y.vocabla.endpoint.http.RESTService
import solutions.s4y.vocabla.id.IdFactory
import solutions.s4y.vocabla.infrastructure.mvstore.KeyValueMVStore.makeMVStoreMemory
import solutions.s4y.vocabla.lang.app.repo.LangRepository
import solutions.s4y.vocabla.lang.infra.langRoRepository
import solutions.s4y.vocabla.words.app.MVStoreWordsService
import solutions.s4y.vocabla.words.app.ports.EntryService
import zio.http.Server
import zio.logging.LogFilter.LogLevelByNameConfig
import zio.logging.{ConsoleLoggerConfig, LogFormat, consoleLogger}
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
  given LangRepository = langRoRepository
  given IdentifierSchema = IdentifierSchema[UUID]

  override val bootstrap: ZLayer[ZIOAppArgs, Config.Error, Unit] =
    super.bootstrap ++ consoleDebugLogger

  // create in-memory MV Store
  private def layerMVStore: ZLayer[Any, String, MVStore] =
    ZLayer.scoped(
      makeMVStoreMemory().mapError(th =>
        s"Failed to create MVStore: ${th.getMessage}"
      )
    )

  private type MVStoreID = UUID
  // id generator
  private def layerIdFactory: ULayer[IdFactory[MVStoreID]] =
    ZLayer.succeed(IdFactory.uuid)
  // finally end up with EntryService implementation
  private val wordsServiceLayer: ZLayer[Any, String, EntryService] =
    (layerIdFactory ++ layerMVStore) >>> MVStoreWordsService
      .makeLayer[MVStoreID]

  private val program: ZIO[
    Scope & EntryService & RESTService & Server,
    String,
    Unit
  ] = {
    for {
      _ <- ZIO.logDebug("Starting app")
      restService <- ZIO.service[RESTService]
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
        RESTService.makeLayer() ++ wordsServiceLayer
    )

  // Server.default.mapError(th => th.toString)
