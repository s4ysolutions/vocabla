package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.domain.model.{IdentifiedEntity, Identity}
import solutions.s4y.vocabla.words.domain.model.{
  Owner,
  Tag
}
import zio.IO

trait TagRepository:
  def addTag(owner: Identity[Owner], label: String): IO[String, Identity[Tag]]

  def getTagsForOwner(
      owner: Identity[Owner]
  ): IO[String, Seq[IdentifiedEntity[Tag]]]
