package solutions.s4y.vocabla.words.app.repo

import zio.IO

trait TagRepository[OwnerID, TagID, TagDTO]:
  def addTag(
      ownerId: OwnerID,
      label: String
  ): IO[String, TagID]

  def getTagsForOwner(ownerId: OwnerID): IO[String, Seq[TagDTO]]
