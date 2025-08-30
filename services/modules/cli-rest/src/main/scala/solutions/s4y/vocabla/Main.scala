package solutions.s4y.vocabla

import solutions.s4y.vocabla.app.VocablaApp
import solutions.s4y.vocabla.endpoint.http.RESTService
import solutions.s4y.vocabla.infra.pgsql.InfraPgLive
import solutions.s4y.zio.consoleColorTraceLogger
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault:
  private val layer =
    InfraPgLive.layer
      >>> VocablaApp.layer[InfraPgLive.TX]()
      >>> RESTService.layer

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Unit] =
    consoleColorTraceLogger

  private val program: ZIO[
    Scope & RESTService,
    String,
    Unit
  ] = {
    for {
      restService <- ZIO.service[RESTService]
      startedPromise <- restService.start()
      _ <- startedPromise.await
      _ <- ZIO.logInfo("Press Ctrl-C to stop the server")
      _ <- ZIO.never
    } yield ()
  }

  override def run: ZIO[Scope, String, Unit] = {
    ZIO.logDebug("Running Main program") *> ZIO.addFinalizer(
      ZIO.logInfo("Exit Main program")
    ) *> program.provideSome[Scope](layer)
  }.catchAllCause(err => ZIO.logError(s"Main program died: " + err.prettyPrint))
