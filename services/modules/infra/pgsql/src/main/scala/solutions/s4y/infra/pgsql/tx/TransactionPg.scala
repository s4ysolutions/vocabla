package solutions.s4y.infra.pgsql.tx

import solutions.s4y.vocabla.app.repo.tx.{Transaction, TransactionContext}
import solutions.s4y.zio.e
import zio.{IO, ZIO}

import java.sql.Connection

case class TransactionPg(connection: Connection) extends Transaction:
  def rollback(): IO[String, Unit] =
    ZIO.attempt(connection.rollback()).e(_.getMessage)
