package solutions.s4y.vocabla.infra.pgsql

import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.composite.Patterns
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.*
import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.Entry.{Definition, Headword}
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import solutions.s4y.vocabla.domain.{Entry, Lang, Tag, User}
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

  override def get[R](
      ownerId: Identifier[User],
      tagIds: Chunk[Identifier[Tag]] = Chunk.empty,
      langCodes: Chunk[Lang.Code] = Chunk.empty,
      text: Option[String] = None,
      limit: Int = 1000
  )(using
      TransactionContextPg
  ): ZIO[R, InfraFailure, Chunk[Identified[Entry]]] =
    val baseQuery =
      "SELECT e.id, e.word, e.langCode, e.definitions, e.ownerId FROM entries e"

    val (whereClause, joinClause) =
      buildWhereClause(ownerId, tagIds, langCodes, text)
    val finalQuery = s"$baseQuery$joinClause$whereClause ORDER BY e.id LIMIT ?"

    pgSelectMany(
      finalQuery,
      st => {
        var paramIndex = 1

        // Set parameters in the same order as the WHERE clause
        st.setLong(paramIndex, ownerId.as[Long])
        paramIndex += 1

        if (tagIds.nonEmpty) {
          val tagArray = st.getConnection
            .createArrayOf("bigint", tagIds.map(_.as[Long]).toArray)
          st.setArray(paramIndex, tagArray)
          paramIndex += 1
        }

        if (langCodes.nonEmpty) {
          val langArray =
            st.getConnection.createArrayOf("text", langCodes.toArray)
          st.setArray(paramIndex, langArray)
          paramIndex += 1
        }

        text.foreach { searchText =>
          st.setString(paramIndex, s"%$searchText%")
          paramIndex += 1
          st.setString(paramIndex, s"%$searchText%")
          paramIndex += 1
        }

        st.setInt(paramIndex, limit)
      },
      rs => {
        val entryId = rs.getLong(1).identifier[Entry]
        val arr: PgArray = rs.getArray(4).asInstanceOf[PgArray]
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

        val entry = Entry(
          Headword(rs.getString(2), rs.getString(3)),
          definitions,
          rs.getLong(5).identifier[User.Student]
        )

        Identified(entryId, entry)
      }
    )

  private def buildWhereClause(
      ownerId: Identifier[User],
      tagIds: Chunk[Identifier[Tag]],
      langCodes: Chunk[Lang.Code],
      text: Option[String]
  ): (String, String) = {
    val conditions = scala.collection.mutable.ListBuffer[String]()
    var joinClause = ""

    conditions += "e.ownerId = ?"

    if (tagIds.nonEmpty) {
      joinClause =
        " INNER JOIN tag_entry_associations tea ON e.id = tea.entry_id"
      conditions += "tea.tag_id = ANY(?)"
    }

    if (langCodes.nonEmpty) {
      conditions += "e.langCode = ANY(?)"
    }

    text.foreach(_ =>
      conditions += "(e.word ILIKE ? OR EXISTS (SELECT 1 FROM unnest(e.definitions) AS def WHERE def.definition ILIKE ?))"
    )

    val whereClause = if (conditions.nonEmpty) {
      " WHERE " + conditions.mkString(" AND ")
    } else {
      ""
    }

    (whereClause, joinClause)
  }

  override def update[R](
      entryId: Identifier[Entry],
      headword: Option[Entry.Headword],
      definitions: Option[Chunk[Entry.Definition]],
      tagIds: Option[Chunk[Identifier[Tag]]]
  )(using TransactionContextPg): IO[InfraFailure, Boolean] =
    val updates = List(
      Option.when(headword.isDefined)("word = ?, langCode = ?"),
      Option.when(definitions.isDefined)("definitions = ?")
    ).flatten

    for {
      entryUpdated <-
        if updates.isEmpty then ZIO.succeed(false)
        else
          val setClause = updates.mkString(", ")
          val sql = s"UPDATE entries SET $setClause WHERE id = ?"

          pgUpdateOne(
            sql,
            st => {
              var paramIndex = 1
              headword.foreach { hw =>
                st.setString(paramIndex, hw.word)
                paramIndex += 1
                st.setString(paramIndex, hw.langCode)
                paramIndex += 1
              }
              definitions.foreach { defs =>
                val defsArray = defs.map { defn =>
                  val obj = new PGobject()
                  obj.setType("definition")
                  obj.setValue(s"(${defn.definition},${defn.langCode})")
                  obj.asInstanceOf[Object]
                }.toArray
                st.setArray(
                  paramIndex,
                  st.getConnection.createArrayOf("definition", defsArray)
                )
                paramIndex += 1
              }
              st.setLong(paramIndex, entryId.as[Long])
            }
          )

      tagsUpdated <- tagIds match
        case Some(tags) =>
          pgUpdateOne(
            "DELETE FROM tag_entry_associations WHERE entry_id = ?",
            st => st.setLong(1, entryId.as[Long])
          ) *>
            ZIO
              .when(tags.nonEmpty) {
                pgUpdateBatch(
                  "INSERT INTO tag_entry_associations (tag_id, entry_id) VALUES (?, ?)",
                  st =>
                    tags.foreach { tagId =>
                      st.setLong(1, tagId.as[Long])
                      st.setLong(2, entryId.as[Long])
                      st.addBatch()
                    }
                ).map(_.sum)
              }
              .map(_.getOrElse(1))
        case None => ZIO.succeed(0)
    } yield entryUpdated || tagsUpdated > 0
end EntryRepositoryPg

object EntryRepositoryPg:
  private val init = Seq(
    """DO $$
    BEGIN
        CREATE TYPE definition AS ( definition TEXT, langCode TEXT );
      EXCEPTION
        WHEN duplicate_object THEN null;
    END
    $$""",
    """CREATE TABLE IF NOT EXISTS entries (
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
