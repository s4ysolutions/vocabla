package solutions.s4y.infra.pgsql.architecture

import solutions.s4y.infra.pgsql.architecture
import zio.test.TestSuccess.Ignored
import zio.test.{Assertion, Spec, TestAspect, ZIOSpecDefault, assert}
import zio.{IO, ULayer, ZIO, ZLayer}

import scala.language.postfixOps

trait Transaction2:
  def rollback(): Unit

class TestTransaction2(private val connection: Connection) extends Transaction2:
  override def rollback(): Unit = connection.rollback()

trait TransactionContext2

case class TestTransactionContext2(connection: Connection)
    extends TransactionContext2:
  override def toString: String = "txImpl"

trait TransactionManager2:
  type T <: Transaction2
  type TX <: TransactionContext2
  def transaction[E, A](
      zio: ZIO[T & TX, Throwable, A]
  ): IO[Throwable, A]

class TestTransactionManager2(private val ds: DataSource)
    extends TransactionManager2:
  override type T = TestTransaction2
  override type TX = TestTransactionContext2
  override def transaction[E, A](
      zio: ZIO[TestTransaction2 & TestTransactionContext2, Throwable, A]
  ): IO[Throwable, A] =
    ZIO.scoped {
      ZIO
        .acquireRelease(
          ZIO
            .attempt {
              val conn = ds.getConnection
              conn.setAutoCommit(false)
              (TestTransactionContext2(conn), TestTransaction2(conn))
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

object TestTransactionManager2:
  val live: ZLayer[DataSource, Nothing, TestTransactionManager2] =
    ZLayer.fromFunction(new TestTransactionManager2(_))

trait Repository2:
  val tm: TransactionManager2
  /*
  type T <: Transaction
  type TX <: TransactionContext*/
  def findById(
      id: String
  ): ZIO[tm.T & tm.TX, Throwable, Option[String]]

class TestRepository2(val tm: TransactionManager2) extends Repository2:
  /*
  override type T = TestTransaction2
  override type TX = TestTransactionContext2
   */
  override def findById(
      id: String
  ): ZIO[tm.TX, Throwable, Option[String]] =
    for {
      tx <- ZIO.service[tm.TX] // { _.connection }
      result <- ZIO.succeed {
        Option(s"Record with id: $id within $tx by {tx.connection}")
      }
    } yield result

object TestRepository2:
  val live: ZLayer[TransactionManager2, Nothing, TestRepository2] =
    ZLayer.fromFunction(new TestRepository2(_))

trait UseCase2:
  def apply(): IO[Throwable, Option[String]]

class TestUseCase2(
    val tm: TransactionManager2,
    val repository: Repository2
) extends UseCase2:
  def apply(): IO[Throwable, Option[String]] =
    tm.transaction {
      // TODO: the problem is to make sure repository.TX == tm.TX
      // TODO: the problem is to make sure repository.tm == tm
      // repository.findById("test-id")
      ZIO.fail(new Exception("Not imlemented"))
    }

object TestUseCase2:
  val live: ZLayer[
    TransactionManager2 & Repository2,
    Nothing,
    UseCase2
  ] =
    ZLayer.fromFunction(new TestUseCase2(_, _))

object ArchitectureSpec2 extends ZIOSpecDefault:
  override def spec: Spec[Any, Throwable] =
    suite("ArchitectureSpec")(
      test("Can create a transaction") {
        for {
          useCase <- ZIO.service[UseCase2]
          result <- useCase()
          _ <- ZIO.debug(s"Result: $result")
        } yield assert(result)(
          Assertion.equalTo(
            Some("Record with id: test-id within txImpl by conn")
          )
        )
      }
    ) .provide {
      val tm: ZLayer[Any, Nothing, TransactionManager2] =
        DataSource.live >>> TestTransactionManager2.live
      val repo: ZLayer[Any, Nothing, TestRepository2] =
        tm >>> TestRepository2.live
      val l: ZLayer[Any, Nothing, UseCase2] =
        (tm ++ repo) >>> TestUseCase2.live
      l
    }  @@ TestAspect.ignore
