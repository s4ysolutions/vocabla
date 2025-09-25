package solutions.s4y.vocabla.infra.pgsql

import org.slf4j.LoggerFactory
import solutions.s4y.infra.pgsql.DataSourcePg
import solutions.s4y.infra.pgsql.tx.TransactionContextPg
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
import zio.{ZIO, ZLayer}

import scala.util.Using

class TagRepositoryPg extends TagRepository[TransactionContextPg]:
  override def create[R](
      tag: Tag
  )(using TransactionContextPg): ZIO[R, InfraFailure, Identifier[Tag]] =
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
  )(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
    pgUpdateOne(
      "UPDATE tags SET label=? WHERE id=?",
      st => {
        st.setString(1, label)
        st.setLong(2, id.as[Long])
      }
    )

  override def delete[R](
      tagId: Identifier[Tag]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Boolean] =
    pgDeleteOne(
      "DELETE FROM tags WHERE id = ?",
      st => st.setLong(1, tagId.as[Long])
    )

  override def get[R](
      tagId: Identifier[Tag]
  )(using TransactionContextPg): ZIO[R, InfraFailure, Option[Tag]] =
    pgSelectOne(
      "SELECT label, ownerId FROM tags WHERE id = ?",
      st => st.setLong(1, tagId.as[Long]),
      rs => Tag(rs.getString(1), rs.getLong(2).identifier[User.Student])
    )

end TagRepositoryPg

object TagRepositoryPg:
  private val init = Seq(
    "CREATE TABLE IF NOT EXISTS tags (id SERIAL PRIMARY KEY, label TEXT NOT NULL, ownerId BIGINT NOT NULL)"
  )
  val layer: ZLayer[DataSourcePg, InfraFailure, TagRepositoryPg] = {
    ZLayer {
      ZIO.logDebug("Initializing TagRepositoryPg...") *>
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
            }.orDie *> ZIO.logDebug("TagRepositoryPg initialized")
          }
          .as(new TagRepositoryPg)
    }
  }
  private val log = LoggerFactory.getLogger(TagRepositoryPg.getClass)
end TagRepositoryPg
