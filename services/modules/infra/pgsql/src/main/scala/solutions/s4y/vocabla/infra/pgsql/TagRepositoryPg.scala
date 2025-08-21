package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.wrappers.executeInsertWithId
import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionContext
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import zio.{ZIO, ZLayer}

class TagRepositoryPg extends TagRepository:
  override def create(
      tag: Tag
  ): ZIO[TransactionContext, String, Identifier[Tag]] =
    executeInsertWithId[Tag](
      TagRepositoryPg.insertSQL,
      st => {
        st.setString(1, tag.label)
        st.setLong(2, tag.ownerId.as[Long])
      }
    )

  override def update(
      tag: Identified[Tag]
  ): ZIO[TransactionContext, String, Boolean] = ???

  override def delete(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContext, String, Boolean] = ???

  override def get(
      tagId: Identifier[Tag]
  ): ZIO[TransactionContext, String, Option[Tag]] = ???

end TagRepositoryPg

object TagRepositoryPg:
  private val init = Seq(
    "DROP TABLE IF EXISTS tags",
    "CREATE TABLE tags (id SERIAL PRIMARY KEY, label TEXT NOT NULL, ownerId BIGINT NOT NULL)"
  )
  private val insertSQL = "INSERT INTO tags (label, ownerId) VALUES (?, ?)"
  val layer: ZLayer[DataSourcePg, String, TagRepository] =
    ZLayer {
      ZIO
        .serviceWithZIO[DataSourcePg] { ds =>
          ZIO.attempt {
            val connection = ds.dataSource.getConnection
            val statement = connection.createStatement()
            init.foreach(statement.execute)
            statement.close()
            connection.close()
          }.orDie *> ZIO.logDebug("TagRepositoryPg initialized")
        }
        .as(new TagRepositoryPg)
    }
end TagRepositoryPg
