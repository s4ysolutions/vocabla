package solutions.s4y.infra.pgsql.tx

import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.zio.e
import zio.{IO, ZIO}

import java.sql.Connection

case class TransactionContextPg(connection: Connection)
    extends TransactionContext
