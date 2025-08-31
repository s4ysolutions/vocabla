package solutions.s4y.vocabla.infra.pgsql

import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.composite.Patterns
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.{
  pgDeleteOne,
  pgInsertWithId,
  pgSelectOne
}
import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.Entry.{Definition, Headword}
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Entry, User}
import zio.{Chunk, IO, ZIO, ZLayer}

import scala.util.Using

class EntryRepositoryPg extends EntryRepository[TransactionContextPg]:

  override def create[R](
      entry: Entry
  )(using TransactionContextPg): IO[InfraFailure, Identifier[Entry]] =
    pgInsertWithId(
      "INSERT INTO entries (word, langCode, definitions, ownerId) VALUES (?, ?, ?, ?)",
      st => {
        val defs: Array[Object] = entry.definitions.map { defn =>
          val obj = new PGobject()
          obj.setType("definition")
          obj.setValue(s"(${defn.definition},${defn.langCode})")
          obj
        }.toArray
        st.setString(1, entry.headword.word)
        st.setString(2, entry.headword.langCode)
        st.setArray(3, st.getConnection.createArrayOf("definition", defs))
        st.setLong(4, entry.ownerId.as[Long])
      }
    )

  override def delete[R](
      entryId: Identifier[Entry]
  )(using TransactionContextPg): IO[InfraFailure, Boolean] =
    pgDeleteOne(
      "DELETE FROM entries WHERE id = ?",
      st => st.setLong(1, entryId.as[Long])
    )

  override def get[R](
      entryId: Identifier[Entry]
  )(using TransactionContextPg): IO[InfraFailure, Option[Entry]] =
    pgSelectOne(
      "SELECT word, langCode, definitions, ownerId FROM entries WHERE id = ?",
      st => st.setLong(1, entryId.as[Long]),
      rs => {
        val arr: PgArray = rs.getArray(3).asInstanceOf[PgArray]
        val defs = arr.getArray.asInstanceOf[Array[Object]]
        val definitions: Chunk[Definition] = Chunk.fromArray(
          defs.map { obj =>
            val pgObj = obj.asInstanceOf[PGobject]
            val value = pgObj.getValue // (definition, langCode)
            value match {
              case Patterns.typeOf2(definition, langCode) =>
                Definition(definition, langCode)
              case _ =>
                throw new Exception(s"Invalid definition format: $value")
            }
          }
        )

        Entry(
          Headword(rs.getString(1), rs.getString(2)),
          definitions,
          rs.getLong(4).identifier[User.Student]
        )
      }
    )

end EntryRepositoryPg

object EntryRepositoryPg:
  private val init = Seq(
    "DROP TABLE IF EXISTS entries CASCADE",
    "DROP TYPE IF EXISTS definition",
    """CREATE TYPE definition AS (
      definition TEXT,
      langCode TEXT
    )""",
    """CREATE TABLE entries (
     id SERIAL PRIMARY KEY,
     word TEXT NOT NULL,
     langCode TEXT NOT NULL,
     definitions definition[],
     ownerId BIGINT NOT NULL
    )"""
  )
  val layer: ZLayer[DataSourcePg, InfraFailure, EntryRepositoryPg] =
    ZLayer {
      ZIO.logDebug("Initializing EntryRepositoryPg...") *>
        ZIO
          .serviceWithZIO[DataSourcePg] { ds =>
            ZIO.attempt {
              Using.Manager { use =>
                val connection = use(ds.dataSource.getConnection)
                val statement = use(connection.createStatement())

                init.foreach { sql =>
                  log.info(s"Executing SQL: $sql")
                  statement.execute(sql)
                }
              }
            }.orDie
          }
          .as(new EntryRepositoryPg) <* ZIO.logDebug(
          "EntryRepositoryPg initialized"
        )
    }
  private val log = LoggerFactory.getLogger(EntryRepositoryPg.getClass)
end EntryRepositoryPg
