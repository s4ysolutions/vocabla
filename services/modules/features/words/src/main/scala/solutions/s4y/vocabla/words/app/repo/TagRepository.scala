package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.domain.model.{IdentifiedEntity, Identifier}
import solutions.s4y.vocabla.words.domain.model.{
  Owner,
  Tag
}
import zio.IO

trait TagRepository:
  def addTag(owner: Identifier[Owner], label: String): IO[String, Identifier[Tag]]

  def getTagsForOwner(
      owner: Identifier[Owner]
  ): IO[String, Seq[IdentifiedEntity[Tag]]]
