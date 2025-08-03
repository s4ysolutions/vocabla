package solutions.s4y.vocabla.tags.app.ports

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import zio.IO

trait DeleteTagUseCase:
  def deleteTag(
      ownerId: Identifier[Owner],
      tagId: Identifier[Tag]
  ): IO[String, Boolean]
