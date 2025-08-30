package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.{TransactionContextPg, TransactionManagerPg}
import solutions.s4y.infra.pgsql.wrappers.{
  pgDeleteOne,
  pgInsertWithId,
  pgSelectOne,
  pgUpdateOne
}
import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.app.repo.error.InfraFailure
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Tag, User}
import zio.{IO, ZIO, ZLayer}

class TagRepositoryPg extends TagRepository[TransactionContextPg]:
  override def create[R](
      tag: Tag
  )(using TransactionContextPg): ZIO[R,InfraFailure, Identifier[Tag]] =
    pgInsertWithId(
      "INSERT INTO tags (label, ownerId) VALUES (?, ?)",
      st => {
        st.setString(1, tag.label)
        st.setLong(2, tag.ownerId.as[Long])
      }
    )

  override def updateLabel[R](
      id: Identifier[Tag],
      label: String
  )(using TransactionContextPg): ZIO[R,InfraFailure, Boolean] =
    pgUpdateOne(
      "UPDATE tags SET label=? WHERE id=?",
      st => {
        st.setString(1, label)
        st.setLong(2, id.as[Long])
      }
    )

  override def delete[R](
      tagId: Identifier[Tag]
  )(using TransactionContextPg): ZIO[R,InfraFailure, Boolean] =
    pgDeleteOne(
      "DELETE FROM tags WHERE id = ?",
      st => st.setLong(1, tagId.as[Long])
    )

  override def get[R](
      tagId: Identifier[Tag]
  )(using TransactionContextPg): ZIO[R,InfraFailure, Option[Tag]] =
    pgSelectOne(
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
  val layer: ZLayer[DataSourcePg, InfraFailure, TagRepositoryPg] =
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
