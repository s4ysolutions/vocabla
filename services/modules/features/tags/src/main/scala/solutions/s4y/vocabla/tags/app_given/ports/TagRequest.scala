package solutions.s4y.vocabla.tags.app_given.ports

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}

enum TagRequest[R]:
  case AddTag(ownerId: Identifier[Owner], tag: Tag)
      extends TagRequest[Identifier[Tag]]
  case RemoveTag(ownerId: Identifier[Owner], tagId: Identifier[Tag])
      extends TagRequest[Boolean]
