package solutions.s4y.vocabla.words.app.repo

import zio.IO

/** A repository interface for managing tags associated with a specific owner.
  *
  * @tparam OwnerID
  *   Type of the owning entity's ID.
  * @tparam TagID
  *   Type of the tag's ID.
  * @tparam TagDTO
  *   Data Transfer Object (DTO) representing a tag. It is a controversial type,
  *   probably TagDTO[TagID] should be used instead
  */
trait TagRepository[OwnerID, TagID, TagDTO]:
  def addTag(
      ownerId: OwnerID,
      label: String
  ): IO[String, TagID]

  def getTagsForOwner(ownerId: OwnerID): IO[String, Seq[TagDTO]]
