package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionPg}
import solutions.s4y.infra.pgsql.wrappers.*
import solutions.s4y.vocabla.app.repo.TagAssociationRepository
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Entry, Tag}
import zio.{Chunk, ZIO, ZLayer}

class TagAssociationRepositoryPg
    extends TagAssociationRepository[
      Entry,
      TransactionPg,
      TransactionContextPg
    ]:

  override def associateTagWithEntry(
      tagId: Identifier[Tag],
      entryId: Identifier[Entry]
  ): ZIO[TransactionContextPg, String, Boolean] =
    pgInsertOne(
      "INSERT INTO tag_entry_associations (tag_id, entry_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
      st => {
        st.setLong(1, tagId.as[Long])
        st.setLong(2, entryId.as[Long])
      }
    )

  override def disassociateTagFromEntry(
      tagId: Identifier[Tag],
      entryId: Identifier[Entry]
  ): ZIO[TransactionContextPg, String, Boolean] =
    pgDelete(
      "DELETE FROM tag_entry_associations WHERE tag_id = ? AND entry_id = ?",
      st => {
        st.setLong(1, tagId.as[Long])
        st.setLong(2, entryId.as[Long])
      }
    )

  override def disassociateTagFromAll(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContextPg, String, Boolean] =
    pgDelete(
      "DELETE FROM tag_entry_associations WHERE tag_id = ?",
      st => st.setLong(1, tagId.as[Long])
    )

  override def disassociateTaggedFromAll(
      entryId: Identifier[Entry]
  ): ZIO[TransactionContextPg, String, Boolean] =
    pgDelete(
      "DELETE FROM tag_entry_associations WHERE entry_id = ?",
      st => st.setLong(1, entryId.as[Long])
    )

  override def getTagged(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContextPg, String, Chunk[Identifier[Entry]]] =
    pgSelectMany(
      "SELECT entry_id FROM tag_entry_associations WHERE tag_id = ?",
      st => st.setLong(1, tagId.as[Long]),
      rs => rs.getLong(1).identifier[Entry]
    )

  override def getTags(
      entryId: Identifier[Entry]
  ): ZIO[TransactionContextPg, String, Chunk[Identifier[Tag]]] =
    pgSelectMany(
      "SELECT tag_id FROM tag_entry_associations WHERE entry_id = ?",
      st => st.setLong(1, entryId.as[Long]),
      rs => rs.getLong(1).identifier[Tag]
    )

end TagAssociationRepositoryPg

object TagAssociationRepositoryPg:
  private val init = Seq(
    "DROP TABLE IF EXISTS tag_entry_associations",
    """CREATE TABLE tag_entry_associations (
       tag_id BIGINT NOT NULL,
       entry_id BIGINT NOT NULL,
       PRIMARY KEY (tag_id, entry_id),
       FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
       FOREIGN KEY (entry_id) REFERENCES entries(id) ON DELETE CASCADE
    )"""
  )

  val layer: ZLayer[DataSourcePg, String, TagAssociationRepositoryPg] =
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
          }.orDie *> ZIO.logDebug("TagAssociationRepositoryPg initialized")
        }
        .as(new TagAssociationRepositoryPg)
    }
end TagAssociationRepositoryPg
