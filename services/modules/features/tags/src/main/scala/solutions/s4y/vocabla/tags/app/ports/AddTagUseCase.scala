package solutions.s4y.vocabla.tags.app.ports

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import zio.IO

trait AddTagUseCase:
  def addTag(ownerId: Identifier[Owner], tag: Tag): IO[String, Identifier[Tag]]
