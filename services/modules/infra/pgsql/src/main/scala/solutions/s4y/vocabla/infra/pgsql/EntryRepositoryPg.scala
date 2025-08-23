package solutions.s4y.vocabla.infra.pgsql

import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.composite.Patterns
import solutions.s4y.infra.pgsql.wrappers.{
  deleteOne,
  insertWithId,
  selectOne,
  updateOne
}
import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.entry.{Definition, Headword}
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Entry, Student}
import zio.{Chunk, ZIO, ZLayer}

class EntryRepositoryPg extends EntryRepository:
  override def create(
      entry: Entry
  ): ZIO[TransactionContext, String, Identifier[Entry]] =
    insertWithId[Entry](
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

  override def delete(
      entryId: Identifier[Entry]
  ): ZIO[TransactionContext, String, Boolean] =
    deleteOne(
      "DELETE FROM entries WHERE id = ?",
      st => st.setLong(1, entryId.as[Long])
    )

  override def get(
      entryId: Identifier[Entry]
  ): ZIO[TransactionContext, String, Option[Entry]] =
    selectOne[Entry](
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
          rs.getLong(4).identifier[Student]
        )
      }
    )

end EntryRepositoryPg

object EntryRepositoryPg:
  private val init = Seq(
    "DROP TABLE IF EXISTS entries",
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
  val layer: ZLayer[DataSourcePg, String, EntryRepository] =
    ZLayer {
      ZIO
        .serviceWithZIO[DataSourcePg] { ds =>
          ZIO.attempt {
            val connection = ds.dataSource.getConnection
            val statement = connection.createStatement()
            init.foreach { sql =>
              statement.execute(sql)
            }
            statement.close()
            connection.close()
          }.orDie *> ZIO.logDebug("EntryRepositoryPg initialized")
        }
        .as(new EntryRepositoryPg)
    }
end EntryRepositoryPg
