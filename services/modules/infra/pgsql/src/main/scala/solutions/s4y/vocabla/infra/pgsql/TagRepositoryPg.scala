package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionPg}
import solutions.s4y.infra.pgsql.wrappers.{
  pgDelete,
  pgInsertWithId,
  pgSelectOne,
  pgUpdateOne
}
import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Tag, User}
import zio.{ZIO, ZLayer}

class TagRepositoryPg
    extends TagRepository[TransactionPg, TransactionContextPg]:
  override def create(
      tag: Tag
  ): ZIO[TransactionContextPg, String, Identifier[Tag]] =
    pgInsertWithId[Tag](
      "INSERT INTO tags (label, ownerId) VALUES (?, ?)",
      st => {
        st.setString(1, tag.label)
        st.setLong(2, tag.ownerId.as[Long])
      }
    )

  override def updateLabel(
      id: Identifier[Tag],
      label: String
  ): ZIO[TransactionContextPg, String, Unit] =
    pgUpdateOne(
      "UPDATE tags SET label=? WHERE id=?",
      st => {
        st.setString(1, label)
        st.setLong(2, id.as[Long])
      }
    )

  override def delete(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContextPg, String, Boolean] =
    pgDelete(
      "DELETE FROM tags WHERE id = ?",
      st => st.setLong(1, tagId.as[Long])
    )

  override def get(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContextPg, String, Option[Tag]] =
    pgSelectOne[Tag](
      "SELECT label, ownerId FROM tags WHERE id = ?",
      st => st.setLong(1, tagId.as[Long]),
      rs => Tag(rs.getString(1), rs.getLong(2).identifier[User.Student])
    )

end TagRepositoryPg

object TagRepositoryPg:
  private val init = Seq(
    "DROP TABLE IF EXISTS tags CASCADE",
    "CREATE TABLE tags (id SERIAL PRIMARY KEY, label TEXT NOT NULL, ownerId BIGINT NOT NULL)"
  )
  val layer: ZLayer[DataSourcePg, String, TagRepositoryPg] =
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
          }.orDie *> ZIO.logDebug("TagRepositoryPg initialized")
        }
        .as(new TagRepositoryPg)
    }
end TagRepositoryPg
