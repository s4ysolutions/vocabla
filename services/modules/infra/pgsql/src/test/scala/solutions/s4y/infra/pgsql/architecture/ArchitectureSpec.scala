package solutions.s4y.infra.pgsql.architecture

import solutions.s4y.infra.pgsql.architecture
import zio.test.{Assertion, Spec, ZIOSpecDefault, assert}
import zio.{IO, ULayer, ZIO, ZLayer}

import scala.language.postfixOps

trait Transaction:
  def rollback(): Unit

class TestTransaction(private val connection: Connection) extends Transaction:
  override def rollback(): Unit = connection.rollback()

trait TransactionContext

case class TestTransactionContext(connection: Connection)
    extends TransactionContext:
  override def toString: String = "txImpl"

trait TransactionManager[T <: Transaction, TX <: TransactionContext]:
  def transaction[E, A](
      zio: ZIO[T & TX, Throwable, A]
  ): IO[Throwable, A]

class TestTransactionManager(private val ds: DataSource)
    extends TransactionManager[TestTransaction, TestTransactionContext]:
  override def transaction[E, A](
      zio: ZIO[TestTransaction & TestTransactionContext, Throwable, A]
  ): IO[Throwable, A] =
    ZIO.scoped {
      ZIO
        .acquireRelease(
          ZIO
            .attempt {
              val conn = ds.getConnection
              conn.setAutoCommit(false)
              (TestTransactionContext(conn), TestTransaction(conn))
            }
        )(t => ZIO.attempt(t._1.connection.close()).orDie)
        .flatMap(t =>
          zio
            .provideSomeLayer(ZLayer.succeed(t._1))
            .provideSomeLayer(ZLayer.succeed(t._2))
            .tapBoth(
              err => ZIO.attempt(t._2.rollback()).orDie,
              _ => ZIO.attempt(t._1.connection.commit()).orDie
            )
        )
    }

object TestTransactionManager:
  val live: ZLayer[DataSource, Nothing, TestTransactionManager] =
    ZLayer.fromFunction(new TestTransactionManager(_))

trait Repository[T <: Transaction, TX <: TransactionContext]:
  def findById(
      id: String
  ): ZIO[T & TX, Throwable, Option[String]]

class TestRepository
    extends Repository[TestTransaction, TestTransactionContext]:
  override def findById(
      id: String
  ): ZIO[TestTransactionContext, Throwable, Option[String]] =
    for {
      tx <- ZIO.service[TestTransactionContext] // { _.connection }
      result <- ZIO.succeed {
        Option(s"Record with id: $id within $tx by ${tx.connection}")
      }
    } yield result

object TestRepository:
  val live: ULayer[TestRepository] = ZLayer.succeed(new TestRepository())

trait UseCase:
  def apply(): IO[Throwable, Option[String]]

class TestUseCase[T <: Transaction, TX <: TransactionContext](
    val tm: TransactionManager[T, TX],
    val repository: Repository[T, TX]
) extends UseCase:
  def apply(): IO[Throwable, Option[String]] =
    tm.transaction {
      repository.findById("test-id")
    }

object TestUseCase:
  type T = TestTransaction
  type TX = TestTransactionContext
  val live: ZLayer[
    TransactionManager[T, TX] & Repository[T, TX],
    Nothing,
    TestUseCase[T, TX]
  ] =
    ZLayer.fromFunction(new TestUseCase[T, TX](_, _))

object ArchitectureSpec extends ZIOSpecDefault:
  override def spec: Spec[Any, Throwable] =
    suite("ArchitectureSpec")(
      test("Can create a transaction") {
        for {
          useCase <- ZIO.service[UseCase]
          result <- useCase()
          _ <- ZIO.debug(s"Result: $result")
        } yield assert(result)(
          Assertion.equalTo(Some("Record with id: test-id within txImpl by conn"))
        )
      }
    ).provide {
      (DataSource.live >>> TestTransactionManager.live ++ TestRepository.live) >>> TestUseCase.live
    }
