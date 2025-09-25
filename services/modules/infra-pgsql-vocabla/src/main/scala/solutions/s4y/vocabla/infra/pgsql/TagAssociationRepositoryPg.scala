package solutions.s4y.vocabla.infra.pgsql

import org.slf4j.LoggerFactory
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
import solutions.s4y.infra.pgsql.wrappers.{
  pgDeleteMany,
  pgDeleteOne,
  pgInsertOne,
  pgSelectMany
}
import solutions.s4y.vocabla.app.repo.TagAssociationRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Entry, Tag}
import zio.{Chunk, ZIO, ZLayer}

import scala.util.Using

class TagAssociationRepositoryPg
    extends TagAssociationRepository[
      Entry,
      TransactionContextPg
    ]:

  override def associateTagWithEntry[R](
      tagId: Identifier[Tag],
      entryId: Identifier[Entry]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
    pgInsertOne(
      "INSERT INTO tag_entry_associations (tag_id, entry_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
      st => {
        st.setLong(1, tagId.as[Long])
        st.setLong(2, entryId.as[Long])
      }
    )

  override def disassociateTagFromEntry[R](
      tagId: Identifier[Tag],
      entryId: Identifier[Entry]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
    pgDeleteOne(
      "DELETE FROM tag_entry_associations WHERE tag_id = ? AND entry_id = ?",
      st => {
        st.setLong(1, tagId.as[Long])
        st.setLong(2, entryId.as[Long])
      }
    )

  override def disassociateTagFromAll[R](
      tagId: Identifier[Tag]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
    pgDeleteMany(
      "DELETE FROM tag_entry_associations WHERE tag_id = ?",
      st => st.setLong(1, tagId.as[Long])
    ).map(_ > 0)

  override def disassociateTaggedFromAll[R](
      entryId: Identifier[Entry]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
    pgDeleteMany(
      "DELETE FROM tag_entry_associations WHERE entry_id = ?",
      st => st.setLong(1, entryId.as[Long])
    ).map(_ > 0)

  override def getTagged[R](
      tagId: Identifier[Tag]
  )(using
      TransactionContextPg
  ): ZIO[R, InfraFailure, Chunk[Identifier[Entry]]] =
    pgSelectMany(
      "SELECT entry_id FROM tag_entry_associations WHERE tag_id = ?",
      st => st.setLong(1, tagId.as[Long]),
      rs => rs.getLong(1).identifier[Entry]
    )

  override def getTags[R](
      entryId: Identifier[Entry]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Chunk[Identifier[Tag]]] =
    pgSelectMany(
      "SELECT tag_id FROM tag_entry_associations WHERE entry_id = ?",
      st => st.setLong(1, entryId.as[Long]),
      rs => rs.getLong(1).identifier[Tag]
    )

end TagAssociationRepositoryPg

object TagAssociationRepositoryPg:
  private val init = Seq(
    """CREATE TABLE IF NOT EXISTS tag_entry_associations (
       tag_id BIGINT NOT NULL,
       entry_id BIGINT NOT NULL,
       PRIMARY KEY (tag_id, entry_id),
       FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
       FOREIGN KEY (entry_id) REFERENCES entries(id) ON DELETE CASCADE
    )"""
  )

  val layer: ZLayer[DataSourcePg, InfraFailure, TagAssociationRepositoryPg] =
    ZLayer {
      ZIO.logDebug("Initializing TagAssociationRepositoryPg...") *>
        ZIO
          .serviceWithZIO[DataSourcePg] { ds =>
            ZIO.fromTry {
              Using.Manager { use =>
                val connection = use(ds.dataSource.getConnection)
                val statement = use(connection.createStatement())

                init.foreach { sql =>
                  log.info(s"Executing SQL: $sql")
                  statement.execute(sql)
                }
              }
            }.orDie *> ZIO.logDebug(
              "TagAssociationRepositoryPg initialized"
            )
          }
          .as(new TagAssociationRepositoryPg)
    }
  private val log = LoggerFactory.getLogger(TagAssociationRepositoryPg.getClass)
end TagAssociationRepositoryPg
