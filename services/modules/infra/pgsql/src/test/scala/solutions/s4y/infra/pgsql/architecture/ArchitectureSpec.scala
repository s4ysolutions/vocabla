package solutions.s4y.infra.pgsql.architecture

import zio.test.{Assertion, ZIOSpecDefault, assert}
import zio.{UIO, ZIO, ZLayer}

import scala.language.postfixOps

trait TransactionManager:
  def transaction[R, E, A](
      zio: ZIO[R & TransactionContext, Throwable, A]
  ): ZIO[R, Throwable, A]

case class TransactionContext(connection: Connection):
  override def toString: String = "tx"

object TransactionManager:
  val postgres: ZLayer[DataSource, Throwable, TransactionManager] =
    ZLayer.fromFunction((ds: DataSource) =>
      new TransactionManager:
        def transaction[R, E, A](
            zio: ZIO[R & TransactionContext, Throwable, A]
        ): ZIO[R, Throwable, A] =
          ZIO.scoped {
            ZIO
              .acquireRelease(
                ZIO
                  .attempt {
                    val conn = ds.getConnection
                    conn.setAutoCommit(false)
                    TransactionContext(conn)
                  }
              )(tx => ZIO.attempt(tx.connection.close()).orDie)
              .flatMap(tx =>
                zio
                  .provideSomeLayer(ZLayer.succeed(tx))
                  .tapBoth(
                    err => ZIO.attempt(tx.connection.rollback()).orDie,
                    _ => ZIO.attempt(tx.connection.commit()).orDie
                  )
              )
          }
    )
trait TestRepository:
  def findById(id: String): ZIO[TransactionContext, Throwable, Option[String]]

object TestRepository:
  val live2: ZLayer[Any, Nothing, TestRepository] =
    ZLayer.succeed(
      new TestRepository:
        override def findById(id: String): UIO[Option[String]] =
          for {
            conn <- ZIO.environmentWith[Any] { env =>
              zio.Unsafe.unsafe { unsafe =>
                env.unsafe.get[Connection](zio.Tag[Connection].tag)(using
                  unsafe
                )
              }
            }
            result <- ZIO.succeed {
              Option(s"Record with id: $id $conn")
            }
          } yield result
    )
  val live: ZLayer[Any, Nothing, TestRepository] =
    ZLayer.succeed(
      new TestRepository:
        override def findById(
            id: String
        ): ZIO[TransactionContext, Throwable, Option[String]] =
          for {
            tx <- ZIO.service[TransactionContext] // { _.connection }
            result <- ZIO.succeed {
              Option(s"Record with id: $id $tx")
            }
          } yield result
    )

val testUseCase
    : ZIO[TransactionManager & TestRepository, Throwable, Option[String]] =
  for {
    tm <- ZIO.service[TransactionManager]
    result <- tm.transaction {
      for {
        repo <- ZIO.service[TestRepository]
        res <- repo.findById("test-id")
      } yield res
    }
  } yield result

object ArchitectureSpec extends ZIOSpecDefault:
  override def spec =
    suite("ArchitectureSpec")(
      test("Can create a transaction") {
        for {
          result <- testUseCase
          _ <- ZIO.debug(s"Result: $result")
        } yield assert(result)(Assertion.equalTo(Some("Record with id: test-id tx")))
      }
    ).provide(
      DataSource.live >>> TransactionManager.postgres ++ TestRepository.live
    )
