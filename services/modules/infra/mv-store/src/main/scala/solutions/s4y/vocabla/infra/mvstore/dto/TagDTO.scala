package solutions.s4y.vocabla.infra.mvstore.dto

import solutions.s4y.vocabla.domain.Tag

final case class TagDTO[OwnerID](
                                  label: String,
                                  ownerId: OwnerID
)

object TagDTO:
  def apply[OwnerID](tag: Tag): TagDTO[OwnerID] =
    TagDTO(tag.label, tag.ownerId.as[OwnerID])

  extension [OwnerID](tag: TagDTO[OwnerID])
    private def asTag: Tag =
      Tag(
        label = tag.label,
        ownerId = solutions.s4y.vocabla.domain.identity
          .Identifier[solutions.s4y.vocabla.domain.Student, OwnerID](
            tag.ownerId
          )
      )
