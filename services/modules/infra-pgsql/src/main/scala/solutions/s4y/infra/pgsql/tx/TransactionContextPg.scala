package solutions.s4y.infra.pgsql.tx

import solutions.s4y.i18n.ResourcesStringsResolver.default
import solutions.s4y.i18n.t
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.app.repo.error.InfraFailure.mapThrowable
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import zio.{IO, ZIO}

import java.sql.Connection

case class TransactionContextPg(connection: Connection)
    extends TransactionContext:
  override def rollback(): IO[InfraFailure, Unit] =
    ZIO
      .attempt(connection.rollback())
      .mapThrowable(t"Failed to rollback transaction")
